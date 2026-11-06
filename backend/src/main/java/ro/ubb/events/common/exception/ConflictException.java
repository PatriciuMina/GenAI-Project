package ro.ubb.events.common.exception;

import org.springframework.http.HttpStatus;

/** 409 for a request that conflicts with existing data, e.g. a duplicate email. */
public class ConflictException extends ApiException {

    public ConflictException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}
