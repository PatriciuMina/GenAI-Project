package ro.ubb.events.event;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ro.ubb.events.event.dto.EventRequest;
import ro.ubb.events.event.entity.EventCategory;

/** Unit test of the validation rules of {@link EventRequest} (field annotations + {@code @ValidEventDates}). */
class EventRequestValidationTest {

    private static final Instant START = Instant.now().plus(Duration.ofDays(30));
    private static final Instant END = START.plus(Duration.ofHours(2));

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @Test
    void validRequestHasNoViolations() {
        assertThat(validator.validate(request(START, END, 40, START.minus(Duration.ofDays(1))))).isEmpty();
    }

    @Test
    void deadlineIsOptional() {
        assertThat(validator.validate(request(START, END, 40, null))).isEmpty();
    }

    @Test
    void deadlineEqualToStartIsAllowed() {
        assertThat(validator.validate(request(START, END, 40, START))).isEmpty();
    }

    @Test
    void endBeforeStartIsReportedOnEndAt() {
        assertThat(fieldsWithErrors(request(START, START.minus(Duration.ofHours(1)), 40, null)))
                .containsExactly("endAt");
    }

    @Test
    void endEqualToStartIsReportedOnEndAt() {
        assertThat(fieldsWithErrors(request(START, START, 40, null))).containsExactly("endAt");
    }

    @Test
    void deadlineAfterStartIsReportedOnRegistrationDeadline() {
        assertThat(fieldsWithErrors(request(START, END, 40, START.plus(Duration.ofMinutes(1)))))
                .containsExactly("registrationDeadline");
    }

    @Test
    void bothDateRulesCanFailTogether() {
        assertThat(fieldsWithErrors(request(START, START, 40, END)))
                .containsExactlyInAnyOrder("endAt", "registrationDeadline");
    }

    @Test
    void startInThePastIsRejected() {
        Instant past = Instant.now().minus(Duration.ofDays(1));
        assertThat(fieldsWithErrors(request(past, past.plus(Duration.ofHours(2)), 40, null)))
                .containsExactly("startAt");
    }

    @Test
    void capacityMustBePositive() {
        assertThat(fieldsWithErrors(request(START, END, 0, null))).containsExactly("capacity");
        assertThat(fieldsWithErrors(request(START, END, -5, null))).containsExactly("capacity");
    }

    @Test
    void missingStartAtGivesOneErrorOnStartAt() {
        assertThat(fieldsWithErrors(request(null, END, 40, END))).containsExactly("startAt");
    }

    @Test
    void titleAndLocationAreRequiredAndLimited() {
        EventRequest blank = new EventRequest(" ", null, "", EventCategory.WORKSHOP, START, END, 40, null);
        assertThat(fieldsWithErrors(blank)).containsExactlyInAnyOrder("title", "location");

        EventRequest tooLong = new EventRequest("x".repeat(201), null, "y".repeat(256),
                EventCategory.WORKSHOP, START, END, 40, null);
        assertThat(fieldsWithErrors(tooLong)).containsExactlyInAnyOrder("title", "location");
    }

    @Test
    void categoryIsRequired() {
        EventRequest noCategory = new EventRequest("Intro to Docker", null, "FSEGA", null, START, END, 40, null);
        assertThat(fieldsWithErrors(noCategory)).containsExactly("category");
    }

    private static EventRequest request(Instant startAt, Instant endAt, Integer capacity, Instant deadline) {
        return new EventRequest("Intro to Docker", "Hands-on workshop", "FSEGA, room 2", EventCategory.WORKSHOP,
                startAt, endAt, capacity, deadline);
    }

    private static Set<String> fieldsWithErrors(EventRequest request) {
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(request);
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
