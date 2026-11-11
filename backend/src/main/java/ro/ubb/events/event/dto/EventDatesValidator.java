package ro.ubb.events.event.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Checks {@link ValidEventDates}. Missing dates are left to {@code @NotNull} on the fields, so a
 * request without {@code startAt} gets one clear error instead of several.
 */
public class EventDatesValidator implements ConstraintValidator<ValidEventDates, EventRequest> {

    @Override
    public boolean isValid(EventRequest request, ConstraintValidatorContext context) {
        if (request == null || request.startAt() == null) {
            return true;
        }
        boolean valid = true;
        context.disableDefaultConstraintViolation();
        if (request.endAt() != null && !request.endAt().isAfter(request.startAt())) {
            context.buildConstraintViolationWithTemplate("must be after startAt")
                    .addPropertyNode("endAt").addConstraintViolation();
            valid = false;
        }
        if (request.registrationDeadline() != null && request.registrationDeadline().isAfter(request.startAt())) {
            context.buildConstraintViolationWithTemplate("must not be after startAt")
                    .addPropertyNode("registrationDeadline").addConstraintViolation();
            valid = false;
        }
        return valid;
    }
}
