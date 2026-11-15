package ro.ubb.events.registration.mapper;

import org.springframework.stereotype.Component;
import ro.ubb.events.registration.dto.ParticipantResponse;
import ro.ubb.events.registration.dto.RegistrationResponse;
import ro.ubb.events.registration.entity.Registration;

/** Entity to DTO conversion for registrations; used only by the service layer. */
@Component
public class RegistrationMapper {

    public RegistrationResponse toResponse(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getCheckedInAt());
    }

    public ParticipantResponse toParticipantResponse(Registration registration) {
        return new ParticipantResponse(
                registration.getId(),
                registration.getUser().getId(),
                registration.getUser().getFullName(),
                registration.getUser().getEmail(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getCheckedInAt());
    }
}
