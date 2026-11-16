package ro.ubb.events.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.service.EventLifecycle;

class EventLifecycleTest {

    private static final Instant START = Instant.parse("2027-01-01T12:00:00Z");
    private static final Instant END = START.plusSeconds(3600);

    @ParameterizedTest
    @MethodSource("allowedTransitions")
    void allowsLifecycleTransitions(EventStatus from, EventStatus to, Instant now) {
        assertThat(EventLifecycle.rejectionReason(from, to, START, END, now)).isNull();
    }

    static Stream<Arguments> allowedTransitions() {
        return Stream.of(
                Arguments.of(EventStatus.DRAFT, EventStatus.PUBLISHED, START.minusSeconds(1)),
                Arguments.of(EventStatus.PUBLISHED, EventStatus.REGISTRATION_CLOSED, START.minusSeconds(7200)),
                Arguments.of(EventStatus.PUBLISHED, EventStatus.ONGOING, START.minusSeconds(3600)),
                Arguments.of(EventStatus.REGISTRATION_CLOSED, EventStatus.ONGOING, START),
                Arguments.of(EventStatus.ONGOING, EventStatus.COMPLETED, END),
                Arguments.of(EventStatus.DRAFT, EventStatus.CANCELLED, START.minusSeconds(7200)),
                Arguments.of(EventStatus.PUBLISHED, EventStatus.CANCELLED, START.minusSeconds(7200)),
                Arguments.of(EventStatus.REGISTRATION_CLOSED, EventStatus.CANCELLED, START),
                Arguments.of(EventStatus.ONGOING, EventStatus.CANCELLED, END));
    }

    @ParameterizedTest
    @MethodSource("rejectedTransitions")
    void rejectsInvalidLifecycleTransitions(EventStatus from, EventStatus to, Instant now) {
        assertThat(EventLifecycle.rejectionReason(from, to, START, END, now)).isNotBlank();
    }

    static Stream<Arguments> rejectedTransitions() {
        return Stream.of(
                Arguments.of(EventStatus.DRAFT, EventStatus.PUBLISHED, START),
                Arguments.of(EventStatus.DRAFT, EventStatus.ONGOING, START),
                Arguments.of(EventStatus.PUBLISHED, EventStatus.ONGOING, START.minusSeconds(3601)),
                Arguments.of(EventStatus.ONGOING, EventStatus.COMPLETED, END.minusSeconds(1)),
                Arguments.of(EventStatus.COMPLETED, EventStatus.CANCELLED, END),
                Arguments.of(EventStatus.CANCELLED, EventStatus.PUBLISHED, END),
                Arguments.of(EventStatus.PUBLISHED, EventStatus.PUBLISHED, END));
    }
}
