package ro.ubb.events.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
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
import ro.ubb.events.organization.service.MembershipService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Unit test: repositories are mocked, the (simple) mapper is real. No Spring, no database. */
@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    private static final Long USER_ID = 7L;
    private static final Long ORG_ID = 1L;

    @Mock
    private OrganizationMembershipRepository membershipRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserService userService;

    private MembershipService membershipService;
    private User user;

    @BeforeEach
    void setUp() {
        membershipService = new MembershipService(
                membershipRepository, organizationRepository, userService, new MembershipMapper());
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

    // --- addMember: POST /organizations/{orgId}/members (T-04) ---

    @Test
    void addMemberSavesMembershipWithRequestedRoleAndReturnsIt() {
        Instant joinedAt = Instant.parse("2026-11-11T09:00:00Z");
        Organization organization = organization(ORG_ID, "CS Society");
        User target = user(20L, "Andrei Toma", "andrei@test.ro");
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization));
        when(userService.findByEmail("andrei@test.ro")).thenReturn(Optional.of(target));
        when(membershipRepository.existsByUserIdAndOrganizationId(20L, ORG_ID)).thenReturn(false);
        when(membershipRepository.saveAndFlush(any(OrganizationMembership.class))).thenAnswer(invocation -> {
            OrganizationMembership toSave = invocation.getArgument(0);
            ReflectionTestUtils.setField(toSave, "id", 42L);
            ReflectionTestUtils.setField(toSave, "joinedAt", joinedAt);
            return toSave;
        });

        MemberResponse result = membershipService.addMember(ORG_ID, new AddMemberRequest("andrei@test.ro", "ORGANIZER"));

        assertThat(result).isEqualTo(new MemberResponse(
                42L, 20L, "Andrei Toma", "andrei@test.ro", OrganizationRole.ORGANIZER, joinedAt));
        ArgumentCaptor<OrganizationMembership> saved = ArgumentCaptor.forClass(OrganizationMembership.class);
        verify(membershipRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getUser()).isSameAs(target);
        assertThat(saved.getValue().getOrganization()).isSameAs(organization);
    }

    @Test
    void addMemberThrowsNotFoundForUnknownOrganization() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.addMember(99L, new AddMemberRequest("andrei@test.ro", "MEMBER")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Organization 99 not found");
        verifyNoInteractions(userService, membershipRepository);
    }

    @Test
    void addMemberThrowsUserNotFoundForUnknownEmail() {
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization(ORG_ID, "CS Society")));
        when(userService.findByEmail("nobody@test.ro")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.addMember(ORG_ID, new AddMemberRequest("nobody@test.ro", "MEMBER")))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(e.getCode()).isEqualTo("USER_NOT_FOUND");
                });
        verify(membershipRepository, never()).saveAndFlush(any());
    }

    @Test
    void addMemberThrowsAlreadyMemberWhenUserIsInTheOrganization() {
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization(ORG_ID, "CS Society")));
        when(userService.findByEmail("ana@test.ro")).thenReturn(Optional.of(user));
        when(membershipRepository.existsByUserIdAndOrganizationId(USER_ID, ORG_ID)).thenReturn(true);

        assertThatThrownBy(() -> membershipService.addMember(ORG_ID, new AddMemberRequest("ana@test.ro", "MEMBER")))
                .isInstanceOfSatisfying(ConflictException.class,
                        e -> assertThat(e.getCode()).isEqualTo("ALREADY_MEMBER"));
        verify(membershipRepository, never()).saveAndFlush(any());
    }

    @Test
    void addMemberMapsUniqueConstraintViolationToAlreadyMember() {
        // two requests add the same user at the same moment: both pass the exists check, the database rejects one
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(organization(ORG_ID, "CS Society")));
        when(userService.findByEmail("ana@test.ro")).thenReturn(Optional.of(user));
        when(membershipRepository.existsByUserIdAndOrganizationId(USER_ID, ORG_ID)).thenReturn(false);
        when(membershipRepository.saveAndFlush(any(OrganizationMembership.class)))
                .thenThrow(new DataIntegrityViolationException("uq_memberships_user_organization"));

        assertThatThrownBy(() -> membershipService.addMember(ORG_ID, new AddMemberRequest("ana@test.ro", "MEMBER")))
                .isInstanceOfSatisfying(ConflictException.class,
                        e -> assertThat(e.getCode()).isEqualTo("ALREADY_MEMBER"));
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
