package ro.ubb.events.event.dto;

import java.time.Instant;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;

/**
 * An event as returned by the API (shared by the W2/W3 endpoints). {@code confirmedCount} is the
 * number of CONFIRMED registrations; {@code freePlaces} = capacity - confirmedCount.
 * {@code version} is needed later to edit the event (optimistic locking, T-11).
 */
public record EventResponse(
        Long id,
        Long organizationId,
        String organizationName,
        String title,
        String description,
        String location,
        EventCategory category,
        Instant startAt,
        Instant endAt,
        Integer capacity,
        Instant registrationDeadline,
        EventStatus status,
        long confirmedCount,
        long freePlaces,
        Long version) {
}
