package ro.ubb.events.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.organization.dto.MemberResponse;
import ro.ubb.events.organization.dto.MembershipResponse;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.mapper.MembershipMapper;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.organization.service.MembershipService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/** Unit test: repositories are mocked, the (simple) mapper is real. No Spring, no database. */
@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long ORG_ID = 1L;

    @Mock
    private OrganizationMembershipRepository membershipRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    private MembershipService membershipService;
    private User user;

    @BeforeEach
    void setUp() {
        membershipService = new MembershipService(membershipRepository, organizationRepository, new MembershipMapper());
        user = user(USER_ID, "Ana", "ana@test.ro");
    }

    // --- findByUserId: GET /users/me/memberships (T-01) ---

    @Test
    void findByUserIdMapsAllFields() {
        Instant joinedAt = Instant.parse("2026-11-06T10:15:30Z");
        OrganizationMembership membership =
                membership(3L, user, organization(1L, "UBB Computer Science Society"), OrganizationRole.PRESIDENT, joinedAt);
        when(membershipRepository.findByUserId(USER_ID)).thenReturn(List.of(membership));

        List<MembershipResponse> result = membershipService.findByUserId(USER_ID);

        assertThat(result).containsExactly(new MembershipResponse(
                3L, 1L, "UBB Computer Science Society", OrganizationRole.PRESIDENT, joinedAt));
    }

    @Test
    void findByUserIdSortsByOrganizationNameIgnoringCase() {
        Instant now = Instant.now();
        when(membershipRepository.findByUserId(USER_ID)).thenReturn(List.of(
                membership(1L, user, organization(10L, "UBB Theatre Club"), OrganizationRole.MEMBER, now),
                membership(2L, user, organization(11L, "art society"), OrganizationRole.ORGANIZER, now),
                membership(3L, user, organization(12L, "Chess Club"), OrganizationRole.PRESIDENT, now)));

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

    // --- findByOrganizationId: GET /organizations/{orgId}/members (T-03) ---

    @Test
    void findByOrganizationIdMapsAllFields() {
        Instant joinedAt = Instant.parse("2026-11-06T10:15:30Z");
        Organization organization = organization(ORG_ID, "UBB Computer Science Society");
        when(organizationRepository.existsById(ORG_ID)).thenReturn(true);
        when(membershipRepository.findByOrganizationId(ORG_ID)).thenReturn(List.of(
                membership(3L, user, organization, OrganizationRole.PRESIDENT, joinedAt)));

        List<MemberResponse> result = membershipService.findByOrganizationId(ORG_ID);

        assertThat(result).containsExactly(new MemberResponse(
                3L, USER_ID, "Ana", "ana@test.ro", OrganizationRole.PRESIDENT, joinedAt));
    }

    @Test
    void findByOrganizationIdSortsByRoleThenFullNameIgnoringCase() {
        Instant now = Instant.now();
        Organization organization = organization(ORG_ID, "CS Society");
        when(organizationRepository.existsById(ORG_ID)).thenReturn(true);
        when(membershipRepository.findByOrganizationId(ORG_ID)).thenReturn(List.of(
                membership(1L, user(11L, "bob", "bob@test.ro"), organization, OrganizationRole.MEMBER, now),
                membership(2L, user(12L, "Zoe", "zoe@test.ro"), organization, OrganizationRole.PRESIDENT, now),
                membership(3L, user(13L, "Alice", "alice@test.ro"), organization, OrganizationRole.MEMBER, now),
                membership(4L, user(14L, "Carl", "carl@test.ro"), organization, OrganizationRole.ORGANIZER, now)));

        List<MemberResponse> result = membershipService.findByOrganizationId(ORG_ID);

        assertThat(result).extracting(MemberResponse::fullName).containsExactly("Zoe", "Carl", "Alice", "bob");
        assertThat(result).extracting(MemberResponse::role).containsExactly(
                OrganizationRole.PRESIDENT, OrganizationRole.ORGANIZER, OrganizationRole.MEMBER, OrganizationRole.MEMBER);
    }

    @Test
    void findByOrganizationIdThrowsNotFoundForUnknownOrganization() {
        when(organizationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> membershipService.findByOrganizationId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Organization 99 not found");
        verifyNoInteractions(membershipRepository);
    }

    private static User user(Long id, String fullName, String email) {
        User user = new User(email, "hash", fullName, GlobalRole.STUDENT);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Organization organization(Long id, String name) {
        Organization organization = new Organization(name, null, null);
        ReflectionTestUtils.setField(organization, "id", id);
        return organization;
    }

    private static OrganizationMembership membership(Long id, User user, Organization organization,
                                                     OrganizationRole role, Instant joinedAt) {
        OrganizationMembership membership = new OrganizationMembership(user, organization, role);
        ReflectionTestUtils.setField(membership, "id", id);
        ReflectionTestUtils.setField(membership, "joinedAt", joinedAt);
        return membership;
    }
}
