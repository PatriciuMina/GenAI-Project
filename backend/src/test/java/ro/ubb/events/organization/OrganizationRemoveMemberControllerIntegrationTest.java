package ro.ubb.events.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.support.IntegrationTestBase;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/**
 * DELETE /api/v1/organizations/{orgId}/members/{membershipId} end to end: who may remove
 * (president / ADMIN), 204, the president cannot be removed (409), wrong organization (404),
 * 401/403. That tasks become unassigned is a database rule, covered by
 * {@code DataModelConstraintsTest.deletingMembershipUnassignsTasks}.
 */
class OrganizationRemoveMemberControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/organizations/{orgId}/members/{membershipId}";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;

    private Organization organization;
    private User president;
    private User organizer;
    private OrganizationMembership presidentMembership;
    private OrganizationMembership organizerMembership;
    private OrganizationMembership memberMembership;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        president = createUser(uniqueEmail("president"), GlobalRole.STUDENT);
        organizer = createUser(uniqueEmail("organizer"), GlobalRole.STUDENT);
        User member = createUser(uniqueEmail("member"), GlobalRole.STUDENT);
        presidentMembership = membershipRepository.save(
                new OrganizationMembership(president, organization, OrganizationRole.PRESIDENT));
        organizerMembership = membershipRepository.save(
                new OrganizationMembership(organizer, organization, OrganizationRole.ORGANIZER));
        memberMembership = membershipRepository.save(
                new OrganizationMembership(member, organization, OrganizationRole.MEMBER));
    }

    @Test
    void presidentRemovesMemberAndTheyDisappearFromTheList() throws Exception {
        String token = token(president);

        remove(organization.getId(), memberMembership.getId(), token).andExpect(status().isNoContent());

        assertThat(membershipRepository.findById(memberMembership.getId())).isEmpty();
        mockMvc.perform(get("/api/v1/organizations/{orgId}/members", organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.membershipId == %d)]", memberMembership.getId()).isEmpty());
    }

    @Test
    void presidentCanRemoveAnOrganizer() throws Exception {
        remove(organization.getId(), organizerMembership.getId(), token(president)).andExpect(status().isNoContent());

        assertThat(membershipRepository.findById(organizerMembership.getId())).isEmpty();
    }

    @Test
    void removingTheSameMemberTwiceReturns404TheSecondTime() throws Exception {
        String token = token(president);
        remove(organization.getId(), memberMembership.getId(), token).andExpect(status().isNoContent());

        remove(organization.getId(), memberMembership.getId(), token)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void thePresidentCannotBeRemoved() throws Exception {
        remove(organization.getId(), presidentMembership.getId(), token(president))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CANNOT_REMOVE_PRESIDENT"));

        assertThat(membershipRepository.findById(presidentMembership.getId())).isPresent();
    }

    @Test
    void membershipOfAnotherOrganizationReturns404AndIsKept() throws Exception {
        Organization other = organizationRepository.save(new Organization("Other " + UUID.randomUUID(), null, null));
        OrganizationMembership otherMembership = membershipRepository.save(new OrganizationMembership(
                createUser(uniqueEmail("other"), GlobalRole.STUDENT), other, OrganizationRole.MEMBER));

        remove(organization.getId(), otherMembership.getId(), token(president))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        assertThat(membershipRepository.findById(otherMembership.getId())).isPresent();
    }

    @Test
    void organizerGets403() throws Exception {
        remove(organization.getId(), memberMembership.getId(), token(organizer))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        assertThat(membershipRepository.findById(memberMembership.getId())).isPresent();
    }

    @Test
    void studentWhoIsNotAMemberGets403() throws Exception {
        remove(organization.getId(), memberMembership.getId(), tokenForNewStudent())
                .andExpect(status().isForbidden());
    }

    @Test
    void adminWhoIsNotAMemberCanRemove() throws Exception {
        remove(organization.getId(), memberMembership.getId(), adminToken()).andExpect(status().isNoContent());
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mockMvc.perform(delete(URL, organization.getId(), memberMembership.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private ResultActions remove(long orgId, long membershipId, String token) throws Exception {
        return mockMvc.perform(delete(URL, orgId, membershipId).header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    private String token(User user) throws Exception {
        return loginAndGetToken(user.getEmail());
    }

    private String adminToken() throws Exception {
        String email = uniqueEmail("admin");
        createUser(email, GlobalRole.ADMIN);
        return loginAndGetToken(email);
    }
}
