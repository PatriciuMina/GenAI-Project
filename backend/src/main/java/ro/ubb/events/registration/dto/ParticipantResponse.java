package ro.ubb.events.registration.dto;

import java.time.Instant;
import ro.ubb.events.registration.entity.RegistrationStatus;

/** One row of the participant list shown to organizers (contains personal data: name and email). */
public record ParticipantResponse(Long registrationId, Long userId, String fullName, String email,
                                  RegistrationStatus status, Instant registeredAt, Instant checkedInAt) { }
