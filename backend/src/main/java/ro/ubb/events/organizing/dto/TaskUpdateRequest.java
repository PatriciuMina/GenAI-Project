package ro.ubb.events.organizing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ro.ubb.events.organizing.entity.TaskStatus;

/**
 * Body of PATCH /events/{eventId}/tasks/{taskId}. Every field is optional and only the ones sent are
 * changed. {@code unassign} (true) removes the assignee and cannot be combined with
 * {@code assigneeMembershipId}.
 */
public record TaskUpdateRequest(
        @Schema(nullable = true, example = "IN_PROGRESS") TaskStatus status,
        @Schema(nullable = true, example = "4") Long assigneeMembershipId,
        @Schema(nullable = true, example = "false") Boolean unassign) { }
