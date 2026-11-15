package ro.ubb.events.registration.mapper;

import org.springframework.stereotype.Component;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.registration.dto.MyRegistrationResponse;
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

    public MyRegistrationResponse toMyResponse(Registration registration) {
        Event event = registration.getEvent();
        return new MyRegistrationResponse(
                registration.getId(),
                event.getId(),
                event.getTitle(),
                event.getStartAt(),
                event.getLocation(),
                event.getStatus(),
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
