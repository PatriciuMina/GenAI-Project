package ro.ubb.events.organizing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.ubb.events.auth.dto.AuthenticatedUser;
import ro.ubb.events.common.dto.ApiError;
import ro.ubb.events.organizing.dto.TaskRequest;
import ro.ubb.events.organizing.dto.TaskResponse;
import ro.ubb.events.organizing.dto.TaskUpdateRequest;
import ro.ubb.events.organizing.service.TaskService;

/** Organizer tasks of an event (W5). */
@RestController
@RequestMapping("/api/v1/events/{eventId}/tasks")
@Tag(name = "Tasks", description = "Organizer tasks of an event (W5)")
@ApiResponse(responseCode = "401", description = "Missing, invalid or expired token",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "List the tasks of an event (organizer or president of its organization)",
            description = "Sorted by due date; tasks without a due date come last.")
    @ApiResponse(responseCode = "200", description = "Tasks of the event")
    @ApiResponse(responseCode = "403", description = "Not an organizer or president of the event's organization",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<TaskResponse> list(
            @PathVariable Long eventId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return taskService.list(eventId, me.id());
    }

    @PatchMapping("/{taskId}")
    @Operation(summary = "Change a task's status and/or assignee (organizer or president of the event's organization)",
            description = "Every field is optional and only the ones sent are changed. `unassign: true` removes the "
                    + "assignee and cannot be combined with `assigneeMembershipId`. Any status can follow any other. "
                    + "Editing the title, description or due date is not part of this endpoint.")
    @ApiResponse(responseCode = "200", description = "The updated task")
    @ApiResponse(responseCode = "400",
            description = "MALFORMED_REQUEST (unknown status), VALIDATION_FAILED (`unassign` together with an assignee) "
                    + "or ASSIGNEE_NOT_IN_ORGANIZATION",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Not an organizer or president of the event's organization",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Event not found, or the task belongs to another event",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public TaskResponse update(
            @PathVariable Long eventId,
            @PathVariable Long taskId,
            @RequestBody TaskUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return taskService.update(eventId, taskId, request, me.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a task (organizer or president of the event's organization)",
            description = "The task starts as TODO. The optional assignee is an organization membership id and "
                    + "must belong to the event's organization.")
    @ApiResponse(responseCode = "201", description = "Task created")
    @ApiResponse(responseCode = "400",
            description = "VALIDATION_FAILED (title 1-200 characters) or ASSIGNEE_NOT_IN_ORGANIZATION",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Not an organizer or president of the event's organization",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public TaskResponse create(
            @PathVariable Long eventId,
            @Valid @RequestBody TaskRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return taskService.create(eventId, request, me.id());
    }
}
