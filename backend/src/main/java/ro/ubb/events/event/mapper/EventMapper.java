package ro.ubb.events.event.mapper;

import org.springframework.stereotype.Component;
import ro.ubb.events.event.dto.EventResponse;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.organization.entity.Organization;

/** Converts events to DTOs. Call inside a transaction: the organization is a lazy relation. */
@Component
public class EventMapper {

    /**
     * @param confirmedCount number of CONFIRMED registrations of the event; the caller counts them
     *                       (the event package does not read registrations itself)
     */
    public EventResponse toResponse(Event event, long confirmedCount) {
        Organization organization = event.getOrganization();
        return new EventResponse(
                event.getId(),
                organization.getId(),
                organization.getName(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getCategory(),
                event.getStartAt(),
                event.getEndAt(),
                event.getCapacity(),
                event.getRegistrationDeadline(),
                event.getStatus(),
                confirmedCount,
                Math.max(0, event.getCapacity() - confirmedCount),
                event.getVersion());
    }
}
