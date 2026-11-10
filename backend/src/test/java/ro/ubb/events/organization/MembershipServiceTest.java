package ro.ubb.events.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ro.ubb.events.organization.dto.MembershipResponse;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.mapper.MembershipMapper;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.service.MembershipService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/** Unit test: repository is mocked, the (simple) mapper is real. No Spring, no database. */
@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    private static final Long USER_ID = 7L;

    @Mock
    private OrganizationMembershipRepository membershipRepository;

    private MembershipService membershipService;
    private User user;

    @BeforeEach
    void setUp() {
        membershipService = new MembershipService(membershipRepository, new MembershipMapper());
        user = new User("ana@test.ro", "hash", "Ana", GlobalRole.STUDENT);
    }

    @Test
    void findByUserIdMapsAllFields() {
        Instant joinedAt = Instant.parse("2026-11-06T10:15:30Z");
        OrganizationMembership membership =
                membership(3L, organization(1L, "UBB Computer Science Society"), OrganizationRole.PRESIDENT, joinedAt);
        when(membershipRepository.findByUserId(USER_ID)).thenReturn(List.of(membership));

        List<MembershipResponse> result = membershipService.findByUserId(USER_ID);

        assertThat(result).containsExactly(new MembershipResponse(
                3L, 1L, "UBB Computer Science Society", OrganizationRole.PRESIDENT, joinedAt));
    }

    @Test
    void findByUserIdSortsByOrganizationNameIgnoringCase() {
        Instant now = Instant.now();
        when(membershipRepository.findByUserId(USER_ID)).thenReturn(List.of(
                membership(1L, organization(10L, "UBB Theatre Club"), OrganizationRole.MEMBER, now),
                membership(2L, organization(11L, "art society"), OrganizationRole.ORGANIZER, now),
                membership(3L, organization(12L, "Chess Club"), OrganizationRole.PRESIDENT, now)));

        List<MembershipResponse> result = membershipService.findByUserId(USER_ID);

        assertThat(result).extracting(MembershipResponse::organizationName)
                .containsExactly("art society", "Chess Club", "UBB Theatre Club");
        assertThat(result).extracting(MembershipResponse::role)
                .containsExactly(OrganizationRole.ORGANIZER, OrganizationRole.PRESIDENT, OrganizationRole.MEMBER);
    }

    @Test
    void findByUserIdReturnsEmptyListWhenUserHasNoMemberships() {
        when(membershipRepository.findByUserId(USER_ID)).thenReturn(List.of());

        assertThat(membershipService.findByUserId(USER_ID)).isEmpty();
    }

    private static Organization organization(Long id, String name) {
        Organization organization = new Organization(name, null, null);
        ReflectionTestUtils.setField(organization, "id", id);
        return organization;
    }

    private OrganizationMembership membership(Long id, Organization organization, OrganizationRole role,
                                              Instant joinedAt) {
        OrganizationMembership membership = new OrganizationMembership(user, organization, role);
        ReflectionTestUtils.setField(membership, "id", id);
        ReflectionTestUtils.setField(membership, "joinedAt", joinedAt);
        return membership;
    }
}
