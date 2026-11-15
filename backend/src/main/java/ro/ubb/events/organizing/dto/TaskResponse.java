package ro.ubb.events.organizing.dto;

import java.time.Instant;
import ro.ubb.events.organizing.entity.TaskStatus;

/** An organizer task of an event; {@code assignee} is null when nobody is assigned. */
public record TaskResponse(Long id, String title, String description, TaskStatus status, Instant dueAt,
                           TaskAssigneeResponse assignee, Instant createdAt) { }
