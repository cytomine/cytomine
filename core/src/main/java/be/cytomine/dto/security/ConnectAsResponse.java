package be.cytomine.dto.security;

public record ConnectAsResponse(
    String accessToken,
    String refreshToken,
    Long expiresIn,
    Long userId,
    String username,
    String actorUsername
) {
}
