package ro.ubb.events.registration.dto;

import java.time.Instant;
import ro.ubb.events.registration.entity.RegistrationStatus;

/** A registration of the calling user, returned when registering. */
public record RegistrationResponse(Long id, Long eventId, String eventTitle, RegistrationStatus status,
                                   Instant registeredAt, Instant checkedInAt) { }
