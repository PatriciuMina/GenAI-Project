package ro.ubb.events.registration.service;

import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.registration.dto.ParticipantResponse;
import ro.ubb.events.registration.dto.RegistrationResponse;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.mapper.RegistrationMapper;
import ro.ubb.events.registration.repository.RegistrationRepository;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Registrations (W4: register with a capacity limit) and the participant list for organizers (W5). */
@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final OrgPermissionService orgPermissionService;
    private final RegistrationMapper registrationMapper;
    private final UserService userService;

    /**
     * {@code EventService} already depends on this service (confirmed counts), so it is injected
     * lazily here to avoid a constructor injection cycle.
     */
    public RegistrationService(RegistrationRepository registrationRepository, @Lazy EventService eventService,
                               OrgPermissionService orgPermissionService, RegistrationMapper registrationMapper,
                               UserService userService) {
        this.registrationRepository = registrationRepository;
        this.eventService = eventService;
        this.orgPermissionService = orgPermissionService;
        this.registrationMapper = registrationMapper;
        this.userService = userService;
    }

    /**
     * Registers the caller for the event, never exceeding its capacity (no waitlist). Everything runs in one
     * transaction that holds a row lock on the event, so two students taking the last place are serialized.
     * Rules, in this order:
     * <ol>
     *   <li>lock the event row (404 if it does not exist or is a DRAFT);</li>
     *   <li>the event is PUBLISHED and now is not after the registration deadline (or the start if there is
     *       no deadline), else 409 REGISTRATION_CLOSED;</li>
     *   <li>the caller has no CONFIRMED registration yet, else 409 ALREADY_REGISTERED;</li>
     *   <li>confirmed registrations are below the capacity, else 409 EVENT_FULL;</li>
     *   <li>insert a new CONFIRMED row (an earlier CANCELLED row stays as history).</li>
     * </ol>
     */
    @Transactional
    public RegistrationResponse register(Long eventId, Long userId) {
        Event event = eventService.getEntityByIdForUpdate(eventId);
        if (event.getStatus() == EventStatus.DRAFT) {
            throw new NotFoundException("Event " + eventId + " not found");
        }
        Instant closesAt = event.getRegistrationDeadline() != null
                ? event.getRegistrationDeadline() : event.getStartAt();
        if (event.getStatus() != EventStatus.PUBLISHED || Instant.now().isAfter(closesAt)) {
            throw new ConflictException("REGISTRATION_CLOSED", "Registration for this event is closed");
        }
        if (registrationRepository.findByEventIdAndUserIdAndStatus(eventId, userId, RegistrationStatus.CONFIRMED)
                .isPresent()) {
            throw alreadyRegistered();
        }
        if (registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED)
                >= event.getCapacity()) {
            throw new ConflictException("EVENT_FULL", "All places for this event are taken");
        }
        User user = userService.getEntityById(userId);
        try {
            Registration saved = registrationRepository.saveAndFlush(new Registration(event, user));
            return registrationMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // ux_registrations_confirmed_once: a duplicate that slipped past the check above
            throw alreadyRegistered();
        }
    }

    @Transactional(readOnly = true)
    public long countConfirmedByEventId(Long eventId) {
        return registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED);
    }

    /** True if the user has a CONFIRMED or CANCELLED registration for the event. */
    @Transactional(readOnly = true)
    public boolean hasRegistration(Long eventId, Long userId) {
        return registrationRepository.existsByEventIdAndUserId(eventId, userId);
    }

    /**
     * Participants of an event sorted by full name, optionally only those with the given status.
     * Only an ORGANIZER or PRESIDENT of the event's organization may call this (emails are personal data).
     *
     * @throws ro.ubb.events.common.exception.NotFoundException if the event does not exist
     * @throws ApiException 403 FORBIDDEN if the caller is not an organizer of the event's organization
     */
    @Transactional(readOnly = true)
    public List<ParticipantResponse> listParticipants(Long eventId, RegistrationStatus status, Long callerId) {
        Event event = eventService.getEntityById(eventId);
        if (!orgPermissionService.isOrganizer(callerId, event.getOrganization().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not an organizer of this event's organization");
        }
        List<Registration> registrations = status == null
                ? registrationRepository.findParticipants(eventId)
                : registrationRepository.findParticipantsByStatus(eventId, status);
        return registrations.stream().map(registrationMapper::toParticipantResponse).toList();
    }

    private static ConflictException alreadyRegistered() {
        return new ConflictException("ALREADY_REGISTERED", "You are already registered for this event");
    }
}
