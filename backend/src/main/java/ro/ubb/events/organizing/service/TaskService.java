package ro.ubb.events.organizing.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.dto.FieldErrorDto;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.common.exception.ValidationException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.service.MembershipService;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.organizing.dto.TaskRequest;
import ro.ubb.events.organizing.dto.TaskResponse;
import ro.ubb.events.organizing.dto.TaskUpdateRequest;
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
        EventTask task = new EventTask(event, request.title().trim());
        task.setDescription(request.description());
        task.setDueAt(request.dueAt());
        if (request.assigneeMembershipId() != null) {
            task.setAssignee(assigneeOf(event, request.assigneeMembershipId()));
        }
        return taskMapper.toResponse(taskRepository.saveAndFlush(task));
    }

    /**
     * Changes the status and/or the assignee of a task of the event; only the fields sent are changed and
     * an empty request changes nothing. Any status can follow any other (also back to TODO). Organizer or
     * president of the event's organization only.
     *
     * @throws ApiException 403 FORBIDDEN, or 400 ASSIGNEE_NOT_IN_ORGANIZATION
     * @throws NotFoundException if the event does not exist or the task is not a task of this event
     * @throws ValidationException 400 if {@code unassign} is combined with {@code assigneeMembershipId}
     */
    @Transactional
    public TaskResponse update(Long eventId, Long taskId, TaskUpdateRequest request, Long callerId) {
        Event event = loadEventForOrganizer(eventId, callerId);
        EventTask task = taskRepository.findByIdAndEventId(taskId, eventId)
                .orElseThrow(() -> new NotFoundException("Task " + taskId + " not found in event " + eventId));
        boolean unassign = Boolean.TRUE.equals(request.unassign());
        if (unassign && request.assigneeMembershipId() != null) {
            throw new ValidationException("unassign cannot be combined with assigneeMembershipId",
                    List.of(new FieldErrorDto("unassign", "Cannot be combined with assigneeMembershipId")));
        }
        if (unassign) {
            task.setAssignee(null);
        } else if (request.assigneeMembershipId() != null) {
            task.setAssignee(assigneeOf(event, request.assigneeMembershipId()));
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        return taskMapper.toResponse(taskRepository.saveAndFlush(task));
    }

    /** The assignee must be a membership of the event's organization (data-model rule 3). */
    private OrganizationMembership assigneeOf(Event event, Long membershipId) {
        return membershipService.findEntityInOrganization(event.getOrganization().getId(), membershipId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "ASSIGNEE_NOT_IN_ORGANIZATION",
                        "The assignee is not a member of this event's organization"));
    }

    private Event loadEventForOrganizer(Long eventId, Long callerId) {
        Event event = eventService.getEntityById(eventId);
        if (!orgPermissionService.isOrganizer(callerId, event.getOrganization().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not an organizer of this event's organization");
        }
        return event;
    }
}
