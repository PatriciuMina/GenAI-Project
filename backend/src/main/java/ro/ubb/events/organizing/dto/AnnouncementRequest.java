package ro.ubb.events.organizing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body of POST /events/{eventId}/announcements. */
public record AnnouncementRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String body) { }
