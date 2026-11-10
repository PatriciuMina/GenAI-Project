package ro.ubb.events.organization.mapper;

import org.springframework.stereotype.Component;
import ro.ubb.events.organization.dto.MembershipResponse;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;

/** Converts memberships to DTOs. Call inside a transaction: the organization is a lazy relation. */
@Component
public class MembershipMapper {

    public MembershipResponse toResponse(OrganizationMembership membership) {
        Organization organization = membership.getOrganization();
        return new MembershipResponse(
                membership.getId(),
                organization.getId(),
                organization.getName(),
                membership.getOrgRole(),
                membership.getJoinedAt());
    }
}
