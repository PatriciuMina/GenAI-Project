package ro.ubb.events.organizing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Body of POST /events/{eventId}/tasks. Description, due date and assignee are optional. */
public record TaskRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        Instant dueAt,
        Long assigneeMembershipId) { }
