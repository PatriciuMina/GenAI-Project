package ro.ubb.events.organization.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.organization.dto.MemberResponse;
import ro.ubb.events.organization.dto.MembershipResponse;
import ro.ubb.events.organization.mapper.MembershipMapper;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.repository.OrganizationRepository;

/** Read operations on organization memberships (W1). Returns DTOs only. */
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
    private final MembershipMapper membershipMapper;

    public MembershipService(OrganizationMembershipRepository membershipRepository,
                             OrganizationRepository organizationRepository,
                             MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
        this.organizationRepository = organizationRepository;
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
}
