package be.cytomine.dto.security;

/**
 * Result of a successful "connect as" request: a regular IAM (Keycloak) access token minted
 * for the target user via token exchange, carrying an {@code act} claim identifying the
 * acting admin.
 */
public record ConnectAsResponse(
    String accessToken,
    String refreshToken,
    Long expiresIn,
    Long userId,
    String username,
    String actorUsername
) {
}
