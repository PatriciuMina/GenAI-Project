package ro.ubb.events.auth.dto;

/**
 * Result of a successful login. Send {@code accessToken} as {@code Authorization: Bearer <accessToken>}.
 *
 * @param accessToken      signed JWT
 * @param tokenType        always "Bearer"
 * @param expiresInSeconds token lifetime in seconds
 */
public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
