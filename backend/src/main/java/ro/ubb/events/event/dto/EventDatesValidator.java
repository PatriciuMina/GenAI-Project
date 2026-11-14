package ro.ubb.events.event.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Instant;

/**
 * Checks {@link ValidEventDates}. Missing dates are left to {@code @NotNull} on the fields, so a
 * request without {@code startAt} gets one clear error instead of several.
 */
public class EventDatesValidator implements ConstraintValidator<ValidEventDates, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Instant startAt;
        Instant endAt;
        Instant registrationDeadline;
        if (value instanceof EventRequest request) {
            startAt = request.startAt();
            endAt = request.endAt();
            registrationDeadline = request.registrationDeadline();
        } else if (value instanceof UpdateEventRequest request) {
            startAt = request.startAt();
            endAt = request.endAt();
            registrationDeadline = request.registrationDeadline();
        } else {
            return true;
        }
        if (startAt == null) {
            return true;
        }
        boolean valid = true;
        context.disableDefaultConstraintViolation();
        if (endAt != null && !endAt.isAfter(startAt)) {
            context.buildConstraintViolationWithTemplate("must be after startAt")
                    .addPropertyNode("endAt").addConstraintViolation();
            valid = false;
        }
        if (registrationDeadline != null && registrationDeadline.isAfter(startAt)) {
            context.buildConstraintViolationWithTemplate("must not be after startAt")
                    .addPropertyNode("registrationDeadline").addConstraintViolation();
            valid = false;
        }
        return valid;
    }
}
