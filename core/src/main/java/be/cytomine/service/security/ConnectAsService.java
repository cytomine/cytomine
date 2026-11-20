package be.cytomine.service.security;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cytomine.common.exceptions.ForbiddenException;
import org.cytomine.common.exceptions.ObjectNotFoundException;
import org.cytomine.common.exceptions.WrongArgumentException;
import org.cytomine.common.repository.model.command.payload.response.UserResponse;
import org.cytomine.common.security.IncomingAuthorizationContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import be.cytomine.domain.security.User;
import be.cytomine.dto.security.ConnectAsResponse;
import be.cytomine.dto.security.TokenExchangeResponse;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.CurrentRoleService;
import be.cytomine.service.CurrentUserService;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ConnectAsService {

    private static final String GRANT_TYPE_TOKEN_EXCHANGE = "urn:ietf:params:oauth:grant-type:token-exchange";
    private static final String ACCESS_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:access_token";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String IMAGE_SERVER_USERNAME = "ImageServer1";

    private final CurrentUserService currentUserService;
    private final CurrentRoleService currentRoleService;
    private final UserRepository userRepository;
    private final Keycloak keycloak;

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri:http://iam:8100/iam/realms/cytomine/protocol/openid-connect/token}")
    private String tokenUri;

    @Value("${keycloak-client.target.realm:cytomine}")
    private String targetRealm;

    @Value("${keycloak-client.target.client-id:core}")
    private String targetClientId;

    private final RestClient restClient = RestClient.create();

    public ConnectAsResponse connectAs(Long targetUserId) {
        UserResponse admin = currentUserService.getCurrentUser();
        if (!currentRoleService.isAdmin(admin)) {
            throw new ForbiddenException("You don't have the right to perform this action! You must be admin!");
        }

        User target = userRepository.findById(targetUserId)
            .orElseThrow(() -> new ObjectNotFoundException("User", targetUserId));

        if (IMAGE_SERVER_USERNAME.equals(target.getUsername())) {
            throw new ForbiddenException("Connect as " + IMAGE_SERVER_USERNAME + " is not allowed");
        }

        String adminJwt = currentAdminJwt();
        String subject = resolveIamSubject(target);
        TokenExchangeResponse exchange = exchange(subject, adminJwt);

        log.info(
            "Connect as: admin {} ({}) now acts as user {} ({})",
            admin.username(), admin.id(), target.getUsername(), target.getId()
        );
        return new ConnectAsResponse(
            exchange.accessToken(),
            exchange.refreshToken(),
            exchange.expiresIn(),
            target.getId(),
            target.getUsername(),
            admin.username()
        );
    }

    public void exit(Long targetUserId) {
        UserResponse admin = currentUserService.getCurrentUser();
        if (!currentRoleService.isAdmin(admin)) {
            throw new ForbiddenException("You don't have the right to perform this action! You must be admin!");
        }

        log.info("Connect as: admin {} ({}) stopped acting as user {}", admin.username(), admin.id(), targetUserId);
    }

    private String resolveIamSubject(User target) {
        if (iamUserMatches(target)) {
            return target.getReference();
        }

        List<UserRepresentation> found = keycloak.realm(targetRealm).users()
            .searchByUsername(target.getUsername(), true);
        if (found.isEmpty()) {
            throw new ForbiddenException(
                "User " + target.getUsername() + " has no IAM account to act as"
            );
        }
        String iamId = found.get(0).getId();
        if (!iamId.equals(target.getReference())) {
            log.info("Connect as: backfilling IAM reference of user {} to {}", target.getUsername(), iamId);
            target.setReference(iamId);
            userRepository.saveAndFlush(target);
        }
        return iamId;
    }

    private boolean iamUserMatches(User target) {
        String reference = target.getReference();
        if (reference == null) {
            return false;
        }
        try {
            UserRepresentation iamUser = keycloak.realm(targetRealm).users()
                .get(reference)
                .toRepresentation();
            return iamUser != null && target.getUsername().equalsIgnoreCase(iamUser.getUsername());
        } catch (NotFoundException | BadRequestException e) {
            return false;
        }
    }

    private String currentAdminJwt() {
        String authorization = IncomingAuthorizationContext.get()
            .map(IncomingAuthorizationContext.Headers::authorization)
            .orElse(null);
        if (authorization == null || authorization.isBlank()) {
            throw new ForbiddenException("Connect as requires a bearer token session");
        }
        if (authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        throw new ForbiddenException("Connect as requires a bearer token session, not an api key");
    }

    private TokenExchangeResponse exchange(String requestedSubject, String subjectToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", GRANT_TYPE_TOKEN_EXCHANGE);
        form.add("client_id", targetClientId);
        form.add("subject_token", subjectToken);
        form.add("subject_token_type", ACCESS_TOKEN_TYPE);
        form.add("requested_subject", requestedSubject);
        try {
            TokenExchangeResponse response = restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(TokenExchangeResponse.class);
            if (response == null || response.accessToken() == null) {
                throw new WrongArgumentException("IAM token exchange returned no access token");
            }
            return response;
        } catch (RestClientResponseException e) {
            log.error("IAM token exchange failed with status {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new WrongArgumentException(
                "IAM denied connect-as (" + describeExchangeFailure(e) + ")."
                    + " Check that admin accounts hold the realm-management 'impersonation' role."
            );
        }
    }

    private String describeExchangeFailure(RestClientResponseException e) {
        try {
            ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            TokenExchangeResponse error =
                mapper.readValue(e.getResponseBodyAsString(), TokenExchangeResponse.class);
            if (error.error() != null) {
                String description = error.errorDescription() != null ? ": " + error.errorDescription() : "";
                return error.error() + description;
            }
        } catch (JsonProcessingException ignored) {
            log.debug("Could not parse token exchange error response", ignored);
        }
        return "HTTP " + e.getStatusCode().value();
    }

}
