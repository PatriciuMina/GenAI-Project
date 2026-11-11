package ro.ubb.events.organization.mapper;

import org.springframework.stereotype.Component;
import ro.ubb.events.organization.dto.MemberResponse;
import ro.ubb.events.organization.dto.MembershipResponse;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.user.entity.User;

/**
 * Converts memberships to DTOs. Call inside a transaction: the organization and the user are
 * lazy relations.
 */
@Component
public class MembershipMapper {

    /** The membership seen from the user's side: which organization, which role. */
    public MembershipResponse toResponse(OrganizationMembership membership) {
        Organization organization = membership.getOrganization();
        return new MembershipResponse(
                membership.getId(),
                organization.getId(),
                organization.getName(),
                membership.getOrgRole(),
                membership.getJoinedAt());
    }

    /** The membership seen from the organization's side: which user, which role. */
    public MemberResponse toMemberResponse(OrganizationMembership membership) {
        User user = membership.getUser();
        return new MemberResponse(
                membership.getId(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                membership.getOrgRole(),
                membership.getJoinedAt());
    }
}
