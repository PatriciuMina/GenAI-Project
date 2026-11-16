package ro.ubb.events.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import ro.ubb.events.common.dto.FieldErrorDto;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.event.dto.EventRequest;
import ro.ubb.events.event.dto.EventResponse;
import ro.ubb.events.event.dto.ChangeStatusRequest;
import ro.ubb.events.event.dto.UpdateEventRequest;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.mapper.EventMapper;
import ro.ubb.events.event.repository.EventRepository;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.service.OrganizationService;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.registration.service.RegistrationService;
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
    private final OrgPermissionService orgPermissionService;
    private final RegistrationService registrationService;

    public EventService(EventRepository eventRepository, OrganizationService organizationService,
                        UserService userService, EventMapper eventMapper,
                        OrgPermissionService orgPermissionService, RegistrationService registrationService) {
        this.eventRepository = eventRepository;
        this.organizationService = organizationService;
        this.userService = userService;
        this.eventMapper = eventMapper;
        this.orgPermissionService = orgPermissionService;
        this.registrationService = registrationService;
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

    /** For other features' services (e.g. registration); entities must not leave the service layer. */
    @Transactional(readOnly = true)
    public Event getEntityById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event " + eventId + " not found"));
    }

    /**
     * Loads the event and locks its row until the caller's transaction ends, so concurrent registrations
     * for the same event are processed one after another. Must be called inside a transaction.
     */
    @Transactional
    public Event getEntityByIdForUpdate(Long eventId) {
        return eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new NotFoundException("Event " + eventId + " not found"));
    }

    /** Returns a published or later event to any authenticated user; drafts are organizer-only. */
    @Transactional(readOnly = true)
    public EventResponse getById(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event " + eventId + " not found"));
        if (event.getStatus() == EventStatus.DRAFT
                && !orgPermissionService.isOrganizer(userId, event.getOrganization().getId())) {
            // Hide the existence of drafts from non-organizers.
            throw new NotFoundException("Event " + eventId + " not found");
        }
        long confirmedCount = registrationService.countConfirmedByEventId(eventId);
        return eventMapper.toResponse(event, confirmedCount);
    }

    /** Updates editable event details, checking event-scoped permission and D11 versioning. */
    @Transactional
    public EventResponse update(Long eventId, UpdateEventRequest request, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event " + eventId + " not found"));
        if (!orgPermissionService.isOrganizer(userId, event.getOrganization().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not an organizer of this event's organization");
        }
        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.PUBLISHED) {
            throw new ConflictException("EVENT_NOT_EDITABLE", "Only DRAFT or PUBLISHED events can be edited");
        }
        if (!event.getVersion().equals(request.version())) {
            throw new ConflictException("CONCURRENT_UPDATE", "Event has changed; reload it before editing");
        }

        long confirmedCount = registrationService.countConfirmedByEventId(eventId);
        if (request.capacity() < confirmedCount) {
            throw new ro.ubb.events.common.exception.ValidationException("Capacity is below confirmed registrations",
                    java.util.List.of(new FieldErrorDto("capacity", "Must be at least " + confirmedCount)));
        }
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setCategory(request.category());
        event.setStartAt(request.startAt());
        event.setEndAt(request.endAt());
        event.setCapacity(request.capacity());
        event.setRegistrationDeadline(request.registrationDeadline());
        try {
            eventRepository.flush();
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new ConflictException("CONCURRENT_UPDATE", "Event has changed; reload it before editing");
        }
        long updatedConfirmedCount = registrationService.countConfirmedByEventId(eventId);
        return eventMapper.toResponse(event, updatedConfirmedCount);
    }

    /** Applies an organizer-requested lifecycle transition and keeps registration history untouched. */
    @Transactional
    public EventResponse changeStatus(Long eventId, ChangeStatusRequest request, Long userId) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new NotFoundException("Event " + eventId + " not found"));
        if (!orgPermissionService.isOrganizer(userId, event.getOrganization().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not an organizer of this event's organization");
        }

        EventStatus from = event.getStatus();
        EventStatus to = request.status();
        String reason = EventLifecycle.rejectionReason(from, to, event.getStartAt(), event.getEndAt(),
                java.time.Instant.now());
        if (reason != null) {
            throw new ConflictException("INVALID_STATUS_TRANSITION",
                    "Cannot transition event from " + from + " to " + to + ": " + reason);
        }

        event.setStatus(to);
        eventRepository.flush();
        return eventMapper.toResponse(event, registrationService.countConfirmedByEventId(eventId));
    }
}
