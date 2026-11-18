package be.cytomine.dto.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * IAM (Keycloak) token endpoint response for an RFC 8693 token exchange, reduced to what
 * impersonation needs. Keycloak returns 200 with access/refresh tokens on success, or 400
 * with error/error_description on policy denial.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TokenExchangeResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("expires_in") Long expiresIn,
    String error,
    @JsonProperty("error_description") String errorDescription
) {
}
