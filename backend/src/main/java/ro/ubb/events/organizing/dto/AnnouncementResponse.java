package ro.ubb.events.organizing.dto;

import java.time.Instant;

public record AnnouncementResponse(Long id, String title, String body, String authorName, Instant createdAt) { }
