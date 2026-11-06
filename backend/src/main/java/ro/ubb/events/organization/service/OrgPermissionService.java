package ro.ubb.events.organization.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;

/**
 * Organization-level permission checks, registered as bean {@code orgPermissions} so they can be
 * used in {@code @PreAuthorize}, for example:
 * <pre>
 * &#64;PreAuthorize("@orgPermissions.isOrganizer(principal.id, #orgId)")
 * &#64;PreAuthorize("hasRole('ADMIN') or @orgPermissions.isPresident(principal.id, #orgId)")
 * </pre>
 * Role hierarchy: PRESIDENT ⊃ ORGANIZER ⊃ MEMBER. A global ADMIN gets no automatic access here;
 * write {@code hasRole('ADMIN') or ...} explicitly where admins should be allowed.
 */
@Service("orgPermissions")
public class OrgPermissionService {

    private final OrganizationMembershipRepository membershipRepository;

    public OrgPermissionService(OrganizationMembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    /** Any role in the organization (MEMBER, ORGANIZER or PRESIDENT). */
    @Transactional(readOnly = true)
    public boolean isMember(Long userId, Long orgId) {
        return roleOf(userId, orgId).isPresent();
    }

    /** ORGANIZER or PRESIDENT. */
    @Transactional(readOnly = true)
    public boolean isOrganizer(Long userId, Long orgId) {
        return roleOf(userId, orgId)
                .map(role -> role == OrganizationRole.ORGANIZER || role == OrganizationRole.PRESIDENT)
                .orElse(false);
    }

    /** PRESIDENT only. */
    @Transactional(readOnly = true)
    public boolean isPresident(Long userId, Long orgId) {
        return roleOf(userId, orgId)
                .map(role -> role == OrganizationRole.PRESIDENT)
                .orElse(false);
    }

    private Optional<OrganizationRole> roleOf(Long userId, Long orgId) {
        if (userId == null || orgId == null) {
            return Optional.empty();
        }
        return membershipRepository.findByUserIdAndOrganizationId(userId, orgId)
                .map(OrganizationMembership::getOrgRole);
    }
}
