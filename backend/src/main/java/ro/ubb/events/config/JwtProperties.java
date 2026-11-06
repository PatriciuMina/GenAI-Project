package ro.ubb.events.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT settings from {@code app.jwt.*} in application.yml (values come from the root .env).
 * Validated at startup: the application refuses to start with a missing or too short secret.
 *
 * @param secret            HMAC-SHA256 signing secret ({@code JWT_SECRET}), at least 32 characters
 * @param expirationMinutes access token lifetime ({@code JWT_EXPIRATION_MINUTES}, default 120)
 */
@Validated
@ConfigurationProperties("app.jwt")
public record JwtProperties(
        @NotBlank(message = "JWT_SECRET must be set (see .env.example)")
        @Size(min = 32, message = "JWT_SECRET must be at least 32 characters")
        String secret,
        @Min(1) long expirationMinutes) {
}
