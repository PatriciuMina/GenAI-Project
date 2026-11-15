package ro.ubb.events.registration.service;

import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.registration.dto.ParticipantResponse;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.mapper.RegistrationMapper;
import ro.ubb.events.registration.repository.RegistrationRepository;

/** Registrations (W4) and the participant list for organizers (W5). */
@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventService eventService;
    private final OrgPermissionService orgPermissionService;
    private final RegistrationMapper registrationMapper;

    /**
     * {@code EventService} already depends on this service (confirmed counts), so it is injected
     * lazily here to avoid a constructor injection cycle.
     */
    public RegistrationService(RegistrationRepository registrationRepository, @Lazy EventService eventService,
                               OrgPermissionService orgPermissionService, RegistrationMapper registrationMapper) {
        this.registrationRepository = registrationRepository;
        this.eventService = eventService;
        this.orgPermissionService = orgPermissionService;
        this.registrationMapper = registrationMapper;
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
}
