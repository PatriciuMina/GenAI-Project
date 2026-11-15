package ro.ubb.events.organizing.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.service.MembershipService;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.organizing.dto.TaskRequest;
import ro.ubb.events.organizing.dto.TaskResponse;
import ro.ubb.events.organizing.entity.EventTask;
import ro.ubb.events.organizing.mapper.TaskMapper;
import ro.ubb.events.organizing.repository.EventTaskRepository;

/** Organizer tasks of an event (W5). Other features' data is read through their services. */
@Service
public class TaskService {

    private final EventTaskRepository taskRepository;
    private final EventService eventService;
    private final OrgPermissionService orgPermissionService;
    private final MembershipService membershipService;
    private final TaskMapper taskMapper;

    public TaskService(EventTaskRepository taskRepository, EventService eventService,
                       OrgPermissionService orgPermissionService, MembershipService membershipService,
                       TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.eventService = eventService;
        this.orgPermissionService = orgPermissionService;
        this.membershipService = membershipService;
        this.taskMapper = taskMapper;
    }

    /**
     * Tasks of the event sorted by due date (tasks without one last). Organizer or president of the event's
     * organization only.
     *
     * @throws ro.ubb.events.common.exception.NotFoundException if the event does not exist
     * @throws ApiException 403 FORBIDDEN if the caller is not an organizer of the event's organization
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> list(Long eventId, Long callerId) {
        loadEventForOrganizer(eventId, callerId);
        return taskRepository.findByEventIdWithAssignee(eventId).stream().map(taskMapper::toResponse).toList();
    }

    /**
     * Creates a task with status TODO. Organizer or president of the event's organization only. If given, the
     * assignee must be a membership of the event's organization.
     *
     * @throws ApiException 403 FORBIDDEN, or 400 ASSIGNEE_NOT_IN_ORGANIZATION
     */
    @Transactional
    public TaskResponse create(Long eventId, TaskRequest request, Long callerId) {
        Event event = loadEventForOrganizer(eventId, callerId);
        OrganizationMembership assignee = null;
        if (request.assigneeMembershipId() != null) {
            assignee = membershipService
                    .findEntityInOrganization(event.getOrganization().getId(), request.assigneeMembershipId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "ASSIGNEE_NOT_IN_ORGANIZATION",
                            "The assignee is not a member of this event's organization"));
        }
        EventTask task = new EventTask(event, request.title().trim());
        task.setDescription(request.description());
        task.setDueAt(request.dueAt());
        task.setAssignee(assignee);
        return taskMapper.toResponse(taskRepository.saveAndFlush(task));
    }

    private Event loadEventForOrganizer(Long eventId, Long callerId) {
        Event event = eventService.getEntityById(eventId);
        if (!orgPermissionService.isOrganizer(callerId, event.getOrganization().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not an organizer of this event's organization");
        }
        return event;
    }
}
