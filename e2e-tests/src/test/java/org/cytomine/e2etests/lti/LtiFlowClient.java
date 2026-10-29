package org.cytomine.e2etests.lti;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Drives the browser side of an LTI 1.3 deep-linking launch at wire level: it performs the
 * login initiation, follows every redirect of the Keycloak flow (including the auto-submitted
 * id_token form from the platform's authorization endpoint), extracts the single-use picker
 * context token from the redirect to the content-selection URL, submits a selection through
 * {@code /lti/dl-return} and finally posts the returned {@code LtiDeepLinkingResponse} JWT to
 * the {@code deep_link_return_url} exactly like a real browser would.
 */
final class LtiFlowClient {

    private static final int MAX_HOPS = 16;

    private static final Pattern FORM_ACTION = Pattern.compile("<form[^>]+action=\"([^\"]+)\"");
    private static final Pattern HIDDEN_INPUT = Pattern.compile("name=\"([A-Za-z_-]+)\" value=\"([^\"]*)\"");

    private final HttpClient client;

    LtiFlowClient() {
        this.client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    }

    /**
     * End of the first leg: the tool redirected the browser to the content-selection URL with
     * the single-use context token of the validated launch.
     */
    record PickerRedirect(String url, String ctxToken) {
    }

    /**
     * End of the flow: the tool answered {@code /lti/dl-return} with an auto-submitting form
     * that posts the {@code LtiDeepLinkingResponse} JWT to the platform.
     */
    record DeepLinkingResponseDelivery(String formAction, String rawJwt) {
    }

    PickerRedirect driveLaunch(LtiPlatformSimulator simulator, Map<String, String> loginInitParameters,
                               Map<String, Object> requestClaims, String contentSelectionUrlPrefix) {
        simulator.beginLaunch(requestClaims, null);
        String startLocation = performLoginInitiation(loginInitParameters);
        String url = startLocation;
        for (int hop = 0; hop < MAX_HOPS; hop++) {
            if (url.startsWith(simulator.authorizationEndpoint())) {
                HttpResponse<String> authResponse = get(url);
                Map<String, String> form = parseAutoSubmitForm(authResponse.body());
                throwIfMissing(form, "id_token", url);
                url = postForm(form.get("action"),
                    Map.of("id_token", form.get("id_token"), "state", form.get("state")));
                continue;
            }
            if (url.startsWith(contentSelectionUrlPrefix)) {
                String contextToken = queryParameter(url, "ctx");
                if (contextToken == null || contextToken.isBlank()) {
                    throw new IllegalStateException("Content-selection redirect without ctx token: " + url);
                }
                return new PickerRedirect(url, contextToken);
            }
            HttpResponse<String> response = get(url);
            if (response.statusCode() >= 300 && response.statusCode() < 400) {
                url = response.headers().firstValue("Location")
                    .orElseThrow(() -> new IllegalStateException("Redirect without a Location header - stopped at "
                        + response.uri()));
                continue;
            }
            throw new IllegalStateException(
                "Unexpected flow stop at " + url + " (" + response.statusCode() + "):\n" + response.body());
        }
        throw new IllegalStateException("LTI login flow did not reach the content-selection URL within "
            + MAX_HOPS + " hops (last url: " + url + ")");
    }

    DeepLinkingResponseDelivery submitSelection(String contextToken, String url, String title, String text,
                                                String customJson) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("ctx", contextToken);
        form.put("url", url);
        form.put("title", title);
        form.put("text", text);
        form.put("custom", customJson);
        HttpResponse<String> dlResponse = postFormNoFollow(LtiSettings.deepLinkReturnUrl(), form);
        if (dlResponse.statusCode() != 200) {
            throw new IllegalStateException("dl-return failed with " + dlResponse.statusCode() + ":\n"
                + dlResponse.body());
        }
        Map<String, String> parsed = parseAutoSubmitForm(dlResponse.body());
        throwIfMissing(parsed, "JWT", LtiSettings.deepLinkReturnUrl());
        return new DeepLinkingResponseDelivery(parsed.get("action"), parsed.get("JWT"));
    }

    /** Replays a selection submission to check that the context token cannot be used again. */
    int replaySelection(String contextToken) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("ctx", contextToken);
        form.put("url", "https://view.example.org/explore/again");
        form.put("custom", "{}");
        HttpResponse<String> posted = postFormNoFollow(LtiSettings.deepLinkReturnUrl(), form);
        return posted.statusCode();
    }

    private String performLoginInitiation(Map<String, String> parameters) {
        boolean asGet = "GET".equalsIgnoreCase(parameters.get("method"));
        Map<String, String> bodyParameters = new HashMap<>(parameters);
        bodyParameters.remove("method");
        bodyParameters.remove("alias");
        HttpRequest request;
        if (asGet) {
            String query = bodyParameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
            request = HttpRequest.newBuilder(URI.create(LtiSettings.loginInitUrl() + "?" + query))
                .GET()
                .build();
        } else {
            request = HttpRequest.newBuilder(URI.create(LtiSettings.loginInitUrl()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(encodeForm(bodyParameters)))
                .build();
        }
        HttpResponse<String> response = send(request);
        if (response.statusCode() == 200 && response.body().contains("id_token")) {
            throw new IllegalStateException("Login initiation answered with content: " + response.body());
        }
        if (response.statusCode() != 303) {
            throw new IllegalStateException("Login initiation returned " + response.statusCode() + ":\n"
                + response.body());
        }
        return response.headers().firstValue("Location")
            .orElseThrow(() -> new IllegalStateException("Login initiation did not redirect"));
    }

    private String postForm(String url, Map<String, String> fields) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encodeForm(fields)))
            .build();
        HttpResponse<String> response = send(request);
        // The broker callback typically answers with a redirect into the remaining flow.
        if (response.statusCode() >= 300 && response.statusCode() < 400) {
            return response.headers().firstValue("Location")
                .orElseThrow(() -> new IllegalStateException("Form post without Location after: " + url));
        }
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            throw new IllegalStateException("Form post to " + url + " stopped the flow:\n" + response.body());
        }
        throw new IllegalStateException("Form post to " + url + " returned " + response.statusCode() + ":\n"
            + response.body());
    }

    private HttpResponse<String> postFormNoFollow(String url, Map<String, String> fields) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(encodeForm(fields)))
            .build();
        return send(request);
    }

    private HttpResponse<String> get(String url) {
        return send(HttpRequest.newBuilder(URI.create(url)).GET().build());
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("HTTP call failed for " + request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("HTTP call interrupted for " + request.uri(), e);
        }
    }

    private static Map<String, String> parseAutoSubmitForm(String html) {
        Map<String, String> fields = new LinkedHashMap<>();
        Matcher action = FORM_ACTION.matcher(html);
        if (action.find()) {
            fields.put("action", action.group(1));
        }
        Matcher inputs = HIDDEN_INPUT.matcher(html);
        while (inputs.find()) {
            fields.put(inputs.group(1), inputs.group(2));
        }
        return fields;
    }

    private static void throwIfMissing(Map<String, String> fields, String name, String context) {
        if (!fields.containsKey(name) || fields.get(name).isBlank()) {
            throw new IllegalStateException("Auto-submitting form with no " + name + " input at: " + context);
        }
    }

    private static String queryParameter(String url, String name) {
        String raw = URI.create(url).getRawQuery();
        if (raw == null) {
            return null;
        }
        for (String pair : raw.split("&")) {
            if (pair.startsWith(name + "=")) {
                return java.net.URLDecoder.decode(pair.substring(name.length() + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static String encodeForm(Map<String, String> fields) {
        return fields.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + encode(entry.getValue()))
            .reduce((left, right) -> left + "&" + right)
            .orElse("");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
