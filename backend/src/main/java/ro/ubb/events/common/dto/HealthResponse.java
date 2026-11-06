package ro.ubb.events.common.dto;

import java.time.Instant;

public record HealthResponse(String status, Instant timestamp) {
}
