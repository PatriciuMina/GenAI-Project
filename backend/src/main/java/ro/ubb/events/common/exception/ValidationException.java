package ro.ubb.events.common.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import ro.ubb.events.common.dto.FieldErrorDto;

/** A business-rule validation error with field details. */
public class ValidationException extends ApiException {

    private final List<FieldErrorDto> fieldErrors;

    public ValidationException(String message, List<FieldErrorDto> fieldErrors) {
        super(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public List<FieldErrorDto> getFieldErrors() {
        return fieldErrors;
    }
}
