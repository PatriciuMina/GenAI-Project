package ro.ubb.events.event.service;

import java.time.Instant;
import java.time.Duration;
import ro.ubb.events.event.entity.EventStatus;

/** The allowed event status transitions and their date requirements (W2). */
public final class EventLifecycle {

    private static final Duration ONGOING_EARLY_WINDOW = Duration.ofHours(1);

    private EventLifecycle() {
    }

    /** Returns the reason a transition is rejected, or {@code null} when it is allowed. */
    public static String rejectionReason(EventStatus from, EventStatus to, Instant startAt, Instant endAt,
                                         Instant now) {
        if (from == to) {
            return "the event is already in that status";
        }
        if (to == EventStatus.CANCELLED && from != EventStatus.COMPLETED && from != EventStatus.CANCELLED) {
            return null;
        }
        if (from == EventStatus.DRAFT && to == EventStatus.PUBLISHED) {
            return startAt.isAfter(now) ? null : "startAt must be in the future";
        }
        if (from == EventStatus.PUBLISHED && to == EventStatus.REGISTRATION_CLOSED) {
            return null;
        }
        if ((from == EventStatus.PUBLISHED || from == EventStatus.REGISTRATION_CLOSED)
                && to == EventStatus.ONGOING) {
            return now.compareTo(startAt.minus(ONGOING_EARLY_WINDOW)) >= 0
                    ? null : "the event can become ONGOING only within one hour before startAt";
        }
        if (from == EventStatus.ONGOING && to == EventStatus.COMPLETED) {
            return now.compareTo(endAt) >= 0 ? null : "endAt has not been reached";
        }
        return "that transition is not allowed by the event lifecycle";
    }
}
