package ro.ubb.events.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for business exceptions that map to a specific HTTP status and error code.
 * Feature packages extend this (e.g. a not-found or conflict exception) instead of
 * returning error responses manually.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
