package ro.ubb.events.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.service.EventLifecycle;

class EventLifecycleTest {

    private static final Instant START = Instant.parse("2027-01-01T12:00:00Z");
    private static final Instant END = Instant.parse("2027-01-01T13:00:00Z");

    @ParameterizedTest(name = "{0} -> {1} at {2} is allowed: {3}")
    @CsvSource({
            // From, to, now relative to the event, allowed
            "DRAFT, PUBLISHED, BEFORE_START, true",
            "DRAFT, REGISTRATION_CLOSED, BEFORE_START, false",
            "DRAFT, ONGOING, BEFORE_START, false",
            "DRAFT, COMPLETED, BEFORE_START, false",
            "DRAFT, CANCELLED, BEFORE_START, true",
            "DRAFT, DRAFT, BEFORE_START, false",
            "PUBLISHED, DRAFT, BEFORE_START, false",
            "PUBLISHED, PUBLISHED, BEFORE_START, false",
            "PUBLISHED, REGISTRATION_CLOSED, BEFORE_START, true",
            "PUBLISHED, ONGOING, ONE_HOUR_BEFORE_START, true",
            "PUBLISHED, COMPLETED, BEFORE_START, false",
            "PUBLISHED, CANCELLED, BEFORE_START, true",
            "REGISTRATION_CLOSED, DRAFT, BEFORE_START, false",
            "REGISTRATION_CLOSED, PUBLISHED, BEFORE_START, false",
            "REGISTRATION_CLOSED, REGISTRATION_CLOSED, BEFORE_START, false",
            "REGISTRATION_CLOSED, ONGOING, ONE_HOUR_BEFORE_START, true",
            "REGISTRATION_CLOSED, COMPLETED, BEFORE_START, false",
            "REGISTRATION_CLOSED, CANCELLED, BEFORE_START, true",
            "ONGOING, DRAFT, BEFORE_START, false",
            "ONGOING, PUBLISHED, BEFORE_START, false",
            "ONGOING, REGISTRATION_CLOSED, BEFORE_START, false",
            "ONGOING, ONGOING, BEFORE_START, false",
            "ONGOING, COMPLETED, AT_END, true",
            "ONGOING, CANCELLED, BEFORE_START, true",
            "COMPLETED, DRAFT, BEFORE_START, false",
            "COMPLETED, PUBLISHED, BEFORE_START, false",
            "COMPLETED, REGISTRATION_CLOSED, BEFORE_START, false",
            "COMPLETED, ONGOING, BEFORE_START, false",
            "COMPLETED, COMPLETED, BEFORE_START, false",
            "COMPLETED, CANCELLED, BEFORE_START, false",
            "CANCELLED, DRAFT, BEFORE_START, false",
            "CANCELLED, PUBLISHED, BEFORE_START, false",
            "CANCELLED, REGISTRATION_CLOSED, BEFORE_START, false",
            "CANCELLED, ONGOING, BEFORE_START, false",
            "CANCELLED, COMPLETED, BEFORE_START, false",
            "CANCELLED, CANCELLED, BEFORE_START, false"
    })
    void matchesLifecycleTransitionMatrix(EventStatus from, EventStatus to, String time,
                                         boolean expectedAllowed) {
        String reason = EventLifecycle.rejectionReason(from, to, START, END, now(time));

        if (expectedAllowed) {
            assertThat(reason).isNull();
        } else {
            assertThat(reason).isNotBlank();
        }
    }

    @ParameterizedTest(name = "date rule rejects {0} -> {1} at {2}")
    @CsvSource({
            "DRAFT, PUBLISHED, AT_START",
            "PUBLISHED, ONGOING, TOO_EARLY_FOR_ONGOING",
            "ONGOING, COMPLETED, BEFORE_END"
    })
    void rejectsTransitionsThatViolateDateRules(EventStatus from, EventStatus to, String time) {
        assertThat(EventLifecycle.rejectionReason(from, to, START, END, now(time))).isNotBlank();
    }

    private static Instant now(String time) {
        return switch (time) {
            case "BEFORE_START" -> START.minusSeconds(7200);
            case "ONE_HOUR_BEFORE_START" -> START.minusSeconds(3600);
            case "AT_START" -> START;
            case "TOO_EARLY_FOR_ONGOING" -> START.minusSeconds(3601);
            case "BEFORE_END" -> END.minusSeconds(1);
            case "AT_END" -> END;
            default -> throw new IllegalArgumentException("Unknown fixed test time: " + time);
        };
    }
}
