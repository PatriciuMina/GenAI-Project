package ro.ubb.events.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.service.OrgPermissionService;

@ExtendWith(MockitoExtension.class)
class OrgPermissionServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long ORG_ID = 10L;

    @Mock
    private OrganizationMembershipRepository membershipRepository;

    @InjectMocks
    private OrgPermissionService permissions;

    private void givenRole(OrganizationRole role) {
        OrganizationMembership membership = mock(OrganizationMembership.class);
        when(membership.getOrgRole()).thenReturn(role);
        when(membershipRepository.findByUserIdAndOrganizationId(USER_ID, ORG_ID)).thenReturn(Optional.of(membership));
    }

    @Test
    void presidentHasAllRoles() {
        givenRole(OrganizationRole.PRESIDENT);
        assertThat(permissions.isMember(USER_ID, ORG_ID)).isTrue();
        assertThat(permissions.isOrganizer(USER_ID, ORG_ID)).isTrue();
        assertThat(permissions.isPresident(USER_ID, ORG_ID)).isTrue();
    }

    @Test
    void organizerIsMemberAndOrganizerButNotPresident() {
        givenRole(OrganizationRole.ORGANIZER);
        assertThat(permissions.isMember(USER_ID, ORG_ID)).isTrue();
        assertThat(permissions.isOrganizer(USER_ID, ORG_ID)).isTrue();
        assertThat(permissions.isPresident(USER_ID, ORG_ID)).isFalse();
    }

    @Test
    void memberIsOnlyMember() {
        givenRole(OrganizationRole.MEMBER);
        assertThat(permissions.isMember(USER_ID, ORG_ID)).isTrue();
        assertThat(permissions.isOrganizer(USER_ID, ORG_ID)).isFalse();
        assertThat(permissions.isPresident(USER_ID, ORG_ID)).isFalse();
    }

    @Test
    void nonMemberHasNoRole() {
        when(membershipRepository.findByUserIdAndOrganizationId(USER_ID, ORG_ID)).thenReturn(Optional.empty());
        assertThat(permissions.isMember(USER_ID, ORG_ID)).isFalse();
        assertThat(permissions.isOrganizer(USER_ID, ORG_ID)).isFalse();
        assertThat(permissions.isPresident(USER_ID, ORG_ID)).isFalse();
    }

    @Test
    void nullIdsAreDeniedWithoutQueryingTheDatabase() {
        assertThat(permissions.isMember(null, ORG_ID)).isFalse();
        assertThat(permissions.isOrganizer(USER_ID, null)).isFalse();
        assertThat(permissions.isPresident(null, null)).isFalse();
        verifyNoInteractions(membershipRepository);
    }
}
