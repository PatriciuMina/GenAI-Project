package ro.ubb.events.common.dto;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error body returned by every endpoint on failure.
 *
 * @param timestamp   when the error occurred (UTC)
 * @param status      HTTP status code
 * @param code        stable, machine-readable error code (e.g. {@code VALIDATION_FAILED})
 * @param message     human-readable description
 * @param fieldErrors per-field validation errors; empty when not applicable
 */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        List<FieldErrorDto> fieldErrors) {

    public static ApiError of(int status, String code, String message) {
        return new ApiError(Instant.now(), status, code, message, List.of());
    }

    public static ApiError of(int status, String code, String message, List<FieldErrorDto> fieldErrors) {
        return new ApiError(Instant.now(), status, code, message, List.copyOf(fieldErrors));
    }
}
