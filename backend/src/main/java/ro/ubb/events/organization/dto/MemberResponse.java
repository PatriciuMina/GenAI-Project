package ro.ubb.events.organization.dto;

import java.time.Instant;
import ro.ubb.events.organization.entity.OrganizationRole;

/** One member of an organization, as shown in the organization's members list. */
public record MemberResponse(
        Long membershipId,
        Long userId,
        String fullName,
        String email,
        OrganizationRole role,
        Instant joinedAt) {
}
