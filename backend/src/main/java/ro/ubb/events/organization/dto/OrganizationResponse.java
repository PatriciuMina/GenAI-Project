package ro.ubb.events.organization.dto;

import java.time.Instant;

/** An organization as returned by the API (list and detail). */
public record OrganizationResponse(
        Long id,
        String name,
        String description,
        String contactEmail,
        Instant createdAt) {
}
