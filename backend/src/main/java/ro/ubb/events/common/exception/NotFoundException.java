package ro.ubb.events.common.exception;

import org.springframework.http.HttpStatus;

/** 404 with code {@code NOT_FOUND}, e.g. {@code new NotFoundException("Organization 5 not found")}. */
public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }
}
