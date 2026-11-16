package ro.ubb.events.event.dto;

import jakarta.validation.constraints.NotNull;
import ro.ubb.events.event.entity.EventStatus;

/** Requested target status for a manual event lifecycle transition. */
public record ChangeStatusRequest(@NotNull EventStatus status) {
}
