package org.cytomine.e2etests.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cytomine.common.repository.model.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class CoreApiClient {

    private static final String REALM = "cytomine";
    private static final String CLIENT_ID = "core";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cytomine.api-url:${cytomine.url}}")
    private String coreUrl;

    @Value("${keycloak-client.url}")
    private String iamUrl;

    public String passwordToken(String username, String password) {
        String form = String.join("&",
            "grant_type=password",
            "client_id=" + CLIENT_ID,
            "username=" + encode(username),
            "password=" + encode(password)
        );
        HttpRequest request = HttpRequest.newBuilder(tokenUri())
            .timeout(Duration.ofSeconds(60))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .POST(HttpRequest.BodyPublishers.ofString(form))
            .build();
        HttpResponse<String> response = send(request);
        if (!isSuccess(response.statusCode())) {
            throw new IllegalStateException(
                "Unable to get IAM token for " + username + ": HTTP " + response.statusCode()
                    + " " + response.body()
            );
        }
        String token = json(response.body()).path("access_token").asText(null);
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("IAM token response has no access_token: " + response.body());
        }
        return token;
    }

    public boolean hasClientRole(String username, String password, String clientId, String roleName) {
        JsonNode roles = decodeJwt(passwordToken(username, password))
            .path("resource_access")
            .path(clientId)
            .path("roles");
        if (!roles.isArray()) {
            return false;
        }
        for (JsonNode role : roles) {
            if (roleName.equals(role.asText())) {
                return true;
            }
        }
        return false;
    }

    public long createUser(String adminToken, String username, String password, Role role) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", username);
        payload.put("name", "Selenium " + username);
        payload.put("firstname", "Selenium");
        payload.put("lastname", username);
        payload.put("email", username + "@selenium.test");
        payload.put("role", role.name());
        payload.put("language", "EN");
        payload.put("isDeveloper", false);
        payload.put("password", password);

        HttpResponse<String> response = sendJson(
            "POST",
            coreUri("/api/user.json"),
            adminToken,
            payload
        );
        if (!isSuccess(response.statusCode())) {
            throw new IllegalStateException(
                "Unable to create user " + username + ": HTTP " + response.statusCode() + " " + response.body()
            );
        }
        long userId = json(response.body()).path("data").path("id").asLong();
        if (userId <= 0) {
            throw new IllegalStateException(
                "User creation response has no user id: " + response.body()
            );
        }
        return userId;
    }

    public void updateUserRole(String adminToken, long userId, String username, Role role) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", username);
        payload.put("name", "Selenium " + username);
        payload.put("firstname", "Selenium");
        payload.put("lastname", username);
        payload.put("email", username + "@selenium.test");
        payload.put("language", "EN");
        payload.put("isDeveloper", false);
        payload.put("role", role.name());

        HttpResponse<String> response = sendJson(
            "PUT",
            coreUri("/api/user/" + userId + ".json"),
            adminToken,
            payload
        );
        if (!isSuccess(response.statusCode())) {
            throw new IllegalStateException(
                "Unable to update user " + username + ": HTTP " + response.statusCode() + " " + response.body()
            );
        }
        defineUserRole(adminToken, userId, role);
    }

    public void defineUserRole(String adminToken, long userId, Role role) {
        HttpRequest request = authorized(
            HttpRequest.newBuilder(coreUri("/api/user/" + userId + "/role/" + role.name() + "/define.json")),
            adminToken
        ).PUT(HttpRequest.BodyPublishers.noBody()).timeout(Duration.ofSeconds(60)).build();
        HttpResponse<String> response = send(request);
        if (!isSuccess(response.statusCode())) {
            throw new IllegalStateException(
                "Unable to define role " + role + " for user " + userId + ": HTTP " + response.statusCode()
                    + " " + response.body()
            );
        }
    }

    public void deleteUser(String adminToken, long userId) {
        if (userId <= 0) {
            return;
        }
        HttpRequest request = authorized(
            HttpRequest.newBuilder(coreUri("/api/user/" + userId + ".json")),
            adminToken
        ).DELETE().timeout(Duration.ofSeconds(60)).build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() != 404 && !isSuccess(response.statusCode())) {
            throw new IllegalStateException(
                "Unable to delete user " + userId + ": HTTP " + response.statusCode() + " " + response.body()
            );
        }
    }

    public ApiResponse connectAs(String adminToken, long targetUserId) {
        HttpRequest request = authorized(
            HttpRequest.newBuilder(coreUri("/api/user/" + targetUserId + "/connect-as.json")),
            adminToken
        ).POST(HttpRequest.BodyPublishers.noBody()).timeout(Duration.ofSeconds(60)).build();
        HttpResponse<String> response = send(request);
        return new ApiResponse(response.statusCode(), jsonOrEmpty(response.body()), response.body());
    }

    public JsonNode decodeJwt(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT");
        }
        try {
            return objectMapper.readTree(Base64.getUrlDecoder().decode(parts[1]));
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to decode JWT", e);
        }
    }

    public record ApiResponse(int status, JsonNode body, String rawBody) {
    }

    private HttpResponse<String> sendJson(String method, URI uri, String token, Object payload) {
        HttpRequest.BodyPublisher body;
        try {
            body = HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize request", e);
        }
        HttpRequest.Builder builder = authorized(HttpRequest.newBuilder(uri), token)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .method(method, body)
            .timeout(Duration.ofSeconds(60));
        return send(builder.build());
    }

    private HttpRequest.Builder authorized(HttpRequest.Builder builder, String token) {
        return builder.header(AUTHORIZATION, BEARER_PREFIX + token);
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IllegalStateException("HTTP request failed: " + request.uri(), e);
        }
    }

    private JsonNode json(String body) {
        if (body == null || body.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to parse JSON response: " + body, e);
        }
    }

    private JsonNode jsonOrEmpty(String body) {
        if (body == null || body.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    private URI tokenUri() {
        return URI.create(trimTrailingSlash(iamUrl)
            + "/realms/" + REALM + "/protocol/openid-connect/token");
    }

    private URI coreUri(String path) {
        return URI.create(trimTrailingSlash(coreUrl) + path);
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isSuccess(int status) {
        return status >= 200 && status < 300;
    }
}
