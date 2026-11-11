package ro.ubb.events.event.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cross-field date rules of an {@link EventRequest}: {@code endAt} after {@code startAt}, and
 * {@code registrationDeadline} (if set) not after {@code startAt}. Violations are reported on the
 * field itself ({@code endAt} / {@code registrationDeadline}), so the API returns a normal
 * {@code fieldErrors} entry that a form can show next to that field.
 */
@Documented
@Constraint(validatedBy = EventDatesValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEventDates {

    String message() default "invalid event dates";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
