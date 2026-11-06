package ro.ubb.events.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of {@code POST /api/v1/auth/login}. */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password) {
}
