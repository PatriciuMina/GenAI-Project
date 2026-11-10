package ro.ubb.events.organization.dto;

import java.time.Instant;
import ro.ubb.events.organization.entity.OrganizationRole;

/** One membership of the logged-in user: which organization and which role there. */
public record MembershipResponse(
        Long membershipId,
        Long organizationId,
        String organizationName,
        OrganizationRole role,
        Instant joinedAt) {
}
