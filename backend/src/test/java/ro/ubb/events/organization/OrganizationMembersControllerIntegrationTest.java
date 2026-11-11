package ro.ubb.events.organization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.support.IntegrationTestBase;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/**
 * GET /api/v1/organizations/{orgId}/members end to end: who may see the list (member / ADMIN),
 * 401/403/404, JSON shape and sorting (role, then full name).
 */
class OrganizationMembersControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/organizations/{orgId}/members";
    private static final long UNKNOWN_ORG_ID = 999_999_999L;

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;

    private Organization organization;
    private User president;
    private User member;
    private OrganizationMembership presidentMembership;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        president = createNamedUser("Zoe President");
        member = createNamedUser("bob Member");
        presidentMembership = membershipRepository.save(
                new OrganizationMembership(president, organization, OrganizationRole.PRESIDENT));
        membershipRepository.save(new OrganizationMembership(member, organization, OrganizationRole.MEMBER));
        membershipRepository.save(
                new OrganizationMembership(createNamedUser("Alice Member"), organization, OrganizationRole.MEMBER));
        membershipRepository.save(
                new OrganizationMembership(createNamedUser("Carl Organizer"), organization, OrganizationRole.ORGANIZER));
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mockMvc.perform(get(URL, organization.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void memberSeesAllMembersSortedByRoleThenName() throws Exception {
        mockMvc.perform(get(URL, organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(member.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].membershipId").value(presidentMembership.getId()))
                .andExpect(jsonPath("$[0].userId").value(president.getId()))
                .andExpect(jsonPath("$[0].fullName").value("Zoe President"))
                .andExpect(jsonPath("$[0].email").value(president.getEmail()))
                .andExpect(jsonPath("$[0].role").value("PRESIDENT"))
                .andExpect(jsonPath("$[0].joinedAt").exists())
                .andExpect(jsonPath("$[1].fullName").value("Carl Organizer"))
                .andExpect(jsonPath("$[1].role").value("ORGANIZER"))
                .andExpect(jsonPath("$[2].fullName").value("Alice Member"))
                .andExpect(jsonPath("$[3].fullName").value("bob Member"));
    }

    @Test
    void studentWhoIsNotAMemberGets403() throws Exception {
        mockMvc.perform(get(URL, organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenForNewStudent())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void presidentOfAnotherOrganizationGets403() throws Exception {
        Organization other = organizationRepository.save(new Organization("Other " + UUID.randomUUID(), null, null));
        User otherPresident = createNamedUser("Other President");
        membershipRepository.save(new OrganizationMembership(otherPresident, other, OrganizationRole.PRESIDENT));

        mockMvc.perform(get(URL, organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(otherPresident.getEmail()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminWhoIsNotAMemberGets200() throws Exception {
        mockMvc.perform(get(URL, organization.getId()).header(HttpHeaders.AUTHORIZATION, bearer(adminToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void adminGets404ForUnknownOrganization() throws Exception {
        mockMvc.perform(get(URL, UNKNOWN_ORG_ID).header(HttpHeaders.AUTHORIZATION, bearer(adminToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void studentGets403ForUnknownOrganization() throws Exception {
        // Not a member of an organization that does not exist: the permission check fails first,
        // so non-admins cannot probe which organization ids exist.
        mockMvc.perform(get(URL, UNKNOWN_ORG_ID).header(HttpHeaders.AUTHORIZATION, bearer(tokenForNewStudent())))
                .andExpect(status().isForbidden());
    }

    private User createNamedUser(String fullName) {
        return userRepository.save(
                new User(uniqueEmail("member"), passwordEncoder.encode(PASSWORD), fullName, GlobalRole.STUDENT));
    }

    private String adminToken() throws Exception {
        String email = uniqueEmail("admin");
        createUser(email, GlobalRole.ADMIN);
        return loginAndGetToken(email);
    }
}
