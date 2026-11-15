package ro.ubb.events.organizing.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.organizing.dto.AnnouncementRequest;
import ro.ubb.events.organizing.dto.AnnouncementResponse;
import ro.ubb.events.organizing.entity.Announcement;
import ro.ubb.events.organizing.mapper.AnnouncementMapper;
import ro.ubb.events.organizing.repository.AnnouncementRepository;
import ro.ubb.events.registration.service.RegistrationService;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Announcements of an event (W5). Other features' data is read through their services. */
@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final EventService eventService;
    private final UserService userService;
    private final OrgPermissionService orgPermissionService;
    private final RegistrationService registrationService;
    private final AnnouncementMapper announcementMapper;

    public AnnouncementService(AnnouncementRepository announcementRepository, EventService eventService,
                               UserService userService, OrgPermissionService orgPermissionService,
                               RegistrationService registrationService, AnnouncementMapper announcementMapper) {
        this.announcementRepository = announcementRepository;
        this.eventService = eventService;
        this.userService = userService;
        this.orgPermissionService = orgPermissionService;
        this.registrationService = registrationService;
        this.announcementMapper = announcementMapper;
    }

    /**
     * Announcements of the event, newest first. Visible to members of the event's organization and to
     * users with a CONFIRMED or CANCELLED registration for it.
     *
     * @throws ro.ubb.events.common.exception.NotFoundException if the event does not exist
     * @throws ApiException 403 FORBIDDEN for anyone else
     */
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> list(Long eventId, Long callerId) {
        Event event = eventService.getEntityById(eventId);
        boolean allowed = orgPermissionService.isMember(callerId, event.getOrganization().getId())
                || registrationService.hasRegistration(eventId, callerId);
        if (!allowed) {
            throw forbidden("Only members of the organization and registered participants can read announcements");
        }
        return announcementRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(announcementMapper::toResponse).toList();
    }

    /**
     * Posts an announcement authored by the caller. Only ORGANIZER/PRESIDENT of the event's organization,
     * and not on DRAFT or COMPLETED events.
     *
     * @throws ApiException 403 FORBIDDEN if the caller is not an organizer
     * @throws ConflictException 409 ANNOUNCEMENT_NOT_ALLOWED for DRAFT or COMPLETED events
     */
    @Transactional
    public AnnouncementResponse create(Long eventId, AnnouncementRequest request, Long callerId) {
        Event event = eventService.getEntityById(eventId);
        if (!orgPermissionService.isOrganizer(callerId, event.getOrganization().getId())) {
            throw forbidden("Not an organizer of this event's organization");
        }
        if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.COMPLETED) {
            throw new ConflictException("ANNOUNCEMENT_NOT_ALLOWED",
                    "Announcements cannot be posted on a DRAFT or COMPLETED event");
        }
        User author = userService.getEntityById(callerId);
        Announcement saved = announcementRepository.saveAndFlush(
                new Announcement(event, author, request.title().trim(), request.body()));
        return announcementMapper.toResponse(saved);
    }

    private static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }
}
