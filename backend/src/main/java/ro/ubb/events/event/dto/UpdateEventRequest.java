package ro.ubb.events.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import ro.ubb.events.event.entity.EventCategory;

/** Editable event fields plus the version the caller last read (D11). */
@ValidEventDates
public record UpdateEventRequest(
        @NotBlank @Size(max = 200)
        @Schema(example = "Intro to Docker") String title,
        @Schema(example = "Hands-on workshop", nullable = true) String description,
        @NotBlank @Size(max = 255)
        @Schema(example = "FSEGA, room 2") String location,
        @NotNull @Schema(example = "WORKSHOP") EventCategory category,
        @NotNull @Future @Schema(example = "2026-12-10T16:00:00Z") Instant startAt,
        @NotNull @Schema(example = "2026-12-10T18:00:00Z", description = "Must be after startAt") Instant endAt,
        @NotNull @Positive @Schema(example = "40") Integer capacity,
        @Schema(example = "2026-12-09T22:00:00Z", nullable = true) Instant registrationDeadline,
        @NotNull @PositiveOrZero @Schema(example = "0", description = "Version from the last EventResponse") Long version) {
}
