package ro.ubb.events.registration.dto;

import java.time.Instant;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.registration.entity.RegistrationStatus;

/** One of the caller's registrations together with the event details the "My registrations" page shows. */
public record MyRegistrationResponse(Long id, Long eventId, String eventTitle, Instant eventStartAt,
                                     String eventLocation, EventStatus eventStatus, RegistrationStatus status,
                                     Instant registeredAt, Instant checkedInAt) { }
