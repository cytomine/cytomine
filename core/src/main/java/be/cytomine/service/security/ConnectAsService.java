package be.cytomine.service.security;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import be.cytomine.domain.security.ConnectAsEvent;
import be.cytomine.domain.security.User;
import be.cytomine.dto.security.ConnectAsResponse;
import be.cytomine.dto.security.TokenExchangeResponse;
import be.cytomine.repository.security.ConnectAsEventRepository;
import be.cytomine.repository.security.UserRepository;
import be.cytomine.service.CurrentRoleService;
import be.cytomine.service.CurrentUserService;

/**
 * "Connect as" (issue #1322): lets an administrator act on behalf of another user by
 * exchanging the admin's IAM access token for a regular token of the target user
 * (RFC 8693 token exchange with {@code requested_subject}). The minted token carries an
 * {@code act} claim identifying the admin, is a plain Keycloak token (so core, repository
 * and all downstream services enforce the target user's ACLs), and every session is
 * persisted to {@code connect_as_event} for audit.
 *
 * <p>IAM side prerequisites (one-time realm configuration): admin accounts must hold the
 * realm-management {@code impersonation} role.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ConnectAsService {

    private static final String GRANT_TYPE_TOKEN_EXCHANGE = "urn:ietf:params:oauth:grant-type:token-exchange";
    private static final String ACCESS_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:access_token";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String IMAGE_SERVER_USERNAME = "ImageServer1";

    private final ConnectAsEventRepository connectAsEventRepository;
    private final CurrentUserService currentUserService;
    private final CurrentRoleService currentRoleService;
    private final UserRepository userRepository;
    private final Keycloak keycloak;

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak-client.target.realm}")
    private String targetRealm;

    @Value("${keycloak-client.target.client-id}")
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

        closeOpenEventsForAdmin(admin.id());
        ConnectAsEvent event = new ConnectAsEvent();
        event.setActingAdminId(admin.id());
        event.setTargetUserId(target.getId());
        event.setTargetUsername(target.getUsername());
        event.setStartedAt(new Date());
        connectAsEventRepository.save(event);

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

        Optional<ConnectAsEvent> event =
            connectAsEventRepository.findFirstByActingAdminIdAndTargetUserIdAndEndedAtIsNullOrderByStartedAtDesc(
                admin.id(), targetUserId
            );
        if (event.isEmpty()) {
            // tolerate the target being the acting identity in the current request
            event = connectAsEventRepository
                .findFirstByTargetUserIdAndEndedAtIsNullOrderByStartedAtDesc(targetUserId);
        }
        if (event.isPresent()) {
            ConnectAsEvent open = event.get();
            open.setEndedAt(new Date());
            connectAsEventRepository.save(open);
            log.info(
                "Connect as: admin {} ({}) stopped acting as user {} ({})",
                admin.username(), admin.id(), open.getTargetUsername(), open.getTargetUserId()
            );
        }
    }

    /**
     * Keycloak UUID of the target user: its stored reference, or looked up by username in IAM
     * (and backfilled, like the authentication listener does) when the user never logged in
     * with an IAM account yet.
     */
    private String resolveIamSubject(User target) {
        if (target.getReference() != null) {
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
        log.info("Connect as: backfilling IAM reference of user {} to {}", target.getUsername(), iamId);
        target.setReference(iamId);
        userRepository.saveAndFlush(target);
        return iamId;
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
            // fall through to the generic message
        }
        return "HTTP " + e.getStatusCode().value();
    }

    private void closeOpenEventsForAdmin(Long actingAdminId) {
        // a new connect-as session by the same admin ends any previous one still marked open
        List<ConnectAsEvent> open = connectAsEventRepository.findByActingAdminIdAndEndedAtIsNull(actingAdminId);
        open.forEach(e -> e.setEndedAt(new Date()));
        connectAsEventRepository.saveAll(open);
    }
}
