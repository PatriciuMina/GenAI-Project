package ro.ubb.events.organization.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.organization.dto.AddMemberRequest;
import ro.ubb.events.organization.dto.MemberResponse;
import ro.ubb.events.organization.dto.MembershipResponse;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.mapper.MembershipMapper;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Organization memberships (W1): who is in which organization, with which role. Returns DTOs only. */
@Service
public class MembershipService {

    private static final Comparator<MembershipResponse> BY_ORGANIZATION_NAME =
            Comparator.comparing(MembershipResponse::organizationName, String.CASE_INSENSITIVE_ORDER);

    /**
     * PRESIDENT, ORGANIZER, MEMBER (the declaration order of {@code OrganizationRole}), then by
     * full name ignoring case; the membership id only makes equal names come out in a stable order.
     */
    private static final Comparator<MemberResponse> BY_ROLE_THEN_NAME =
            Comparator.comparing(MemberResponse::role)
                    .thenComparing(MemberResponse::fullName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(MemberResponse::membershipId);

    private final OrganizationMembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final UserService userService;
    private final MembershipMapper membershipMapper;

    public MembershipService(OrganizationMembershipRepository membershipRepository,
                             OrganizationRepository organizationRepository,
                             UserService userService,
                             MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
        this.organizationRepository = organizationRepository;
        this.userService = userService;
        this.membershipMapper = membershipMapper;
    }

    /**
     * All memberships of one user, sorted by organization name (ignoring case).
     * An empty list when the user is in no organization.
     */
    @Transactional(readOnly = true)
    public List<MembershipResponse> findByUserId(Long userId) {
        return membershipRepository.findByUserId(userId).stream()
                .map(membershipMapper::toResponse)
                .sorted(BY_ORGANIZATION_NAME)
                .toList();
    }

    /**
     * All members of one organization, sorted by role (PRESIDENT, ORGANIZER, MEMBER), then by
     * full name. Who may call this is checked in the controller ({@code @PreAuthorize}).
     *
     * @throws NotFoundException if the organization does not exist
     */
    @Transactional(readOnly = true)
    public List<MemberResponse> findByOrganizationId(Long organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new NotFoundException("Organization " + organizationId + " not found");
        }
        return membershipRepository.findByOrganizationId(organizationId).stream()
                .map(membershipMapper::toMemberResponse)
                .sorted(BY_ROLE_THEN_NAME)
                .toList();
    }

    /**
     * Adds an existing account (found by email, ignoring case) to the organization with the
     * requested role. The request is already validated: {@code role} is MEMBER or ORGANIZER.
     * Who may call this is checked in the controller ({@code @PreAuthorize}).
     *
     * @throws NotFoundException if the organization does not exist (404 NOT_FOUND)
     * @throws ApiException 404 USER_NOT_FOUND if no account has this email
     * @throws ConflictException 409 ALREADY_MEMBER if the user is already in the organization
     */
    @Transactional
    public MemberResponse addMember(Long organizationId, AddMemberRequest request) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new NotFoundException("Organization " + organizationId + " not found"));
        User user = userService.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "No account found with email " + request.email()));
        if (membershipRepository.existsByUserIdAndOrganizationId(user.getId(), organizationId)) {
            throw alreadyMember();
        }
        OrganizationRole role = OrganizationRole.valueOf(request.role());
        try {
            OrganizationMembership saved =
                    membershipRepository.saveAndFlush(new OrganizationMembership(user, organization, role));
            return membershipMapper.toMemberResponse(saved);
        } catch (DataIntegrityViolationException e) {
            // two requests adding the same user at the same time: uq_memberships_user_organization wins
            throw alreadyMember();
        }
    }

    /**
     * Removes a membership from the organization. Tasks assigned to it become unassigned in the
     * database ({@code fk_event_tasks_assignee ON DELETE SET NULL}). Who may call this is checked
     * in the controller ({@code @PreAuthorize}).
     *
     * @throws NotFoundException if the membership does not exist or belongs to another organization
     * @throws ConflictException 409 CANNOT_REMOVE_PRESIDENT: an organization always keeps its president
     */
    @Transactional
    public void removeMember(Long organizationId, Long membershipId) {
        OrganizationMembership membership = membershipRepository.findById(membershipId)
                .filter(m -> m.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new NotFoundException(
                        "Membership " + membershipId + " not found in organization " + organizationId));
        if (membership.getOrgRole() == OrganizationRole.PRESIDENT) {
            throw new ConflictException("CANNOT_REMOVE_PRESIDENT", "The president of an organization cannot be removed");
        }
        membershipRepository.delete(membership);
    }

    private static ConflictException alreadyMember() {
        return new ConflictException("ALREADY_MEMBER", "This user is already a member of the organization");
    }
}
