package ro.ubb.events.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.event.dto.EventRequest;
import ro.ubb.events.event.dto.EventResponse;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.mapper.EventMapper;
import ro.ubb.events.event.repository.EventRepository;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.service.OrganizationService;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/**
 * Events and their lifecycle (W2). Returns DTOs only. Other features' data is read through their
 * services ({@link OrganizationService}, {@link UserService}), never their repositories.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository, OrganizationService organizationService,
                        UserService userService, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.organizationService = organizationService;
        this.userService = userService;
        this.eventMapper = eventMapper;
    }

    /**
     * Creates an event of the organization; it always starts as DRAFT (the entity default).
     * The request is already validated (fields and dates). Who may call this is checked in the
     * controller ({@code @PreAuthorize}: organizer or president of the organization).
     *
     * @throws NotFoundException if the organization (or the creator) does not exist
     */
    @Transactional
    public EventResponse create(Long organizationId, EventRequest request, Long creatorId) {
        Organization organization = organizationService.getEntityById(organizationId);
        User creator = userService.getEntityById(creatorId);
        Event event = new Event(organization, creator, request.title(), request.location(), request.category(),
                request.startAt(), request.endAt(), request.capacity());
        event.setDescription(request.description());
        event.setRegistrationDeadline(request.registrationDeadline());
        Event saved = eventRepository.saveAndFlush(event);
        // a new event has no registrations yet
        return eventMapper.toResponse(saved, 0);
    }
}
