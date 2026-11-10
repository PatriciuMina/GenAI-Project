package ro.ubb.events.organization.service;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.organization.dto.MembershipResponse;
import ro.ubb.events.organization.mapper.MembershipMapper;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;

/** Read operations on organization memberships (W1). Returns DTOs only. */
@Service
public class MembershipService {

    private static final Comparator<MembershipResponse> BY_ORGANIZATION_NAME =
            Comparator.comparing(MembershipResponse::organizationName, String.CASE_INSENSITIVE_ORDER);

    private final OrganizationMembershipRepository membershipRepository;
    private final MembershipMapper membershipMapper;

    public MembershipService(OrganizationMembershipRepository membershipRepository, MembershipMapper membershipMapper) {
        this.membershipRepository = membershipRepository;
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
}
