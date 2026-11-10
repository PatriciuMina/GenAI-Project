package ro.ubb.events.organization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
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

/** GET /api/v1/users/me/memberships end to end: security, JSON shape, sorting, only own rows. */
class MyMembershipsControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/users/me/memberships";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;

    @Test
    void withoutTokenReturns401() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void userWithoutMembershipsGetsEmptyList() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer(tokenForNewStudent())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnsOnlyMyMembershipsSortedByOrganizationName() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Organization zeta = organizationRepository.save(new Organization("Zeta Club " + suffix, null, null));
        Organization alpha = organizationRepository.save(new Organization("alpha society " + suffix, null, null));
        Organization other = organizationRepository.save(new Organization("Beta Club " + suffix, null, null));

        String email = uniqueEmail("member");
        User me = createUser(email, GlobalRole.STUDENT);
        User someoneElse = createUser(uniqueEmail("other"), GlobalRole.STUDENT);
        OrganizationMembership zetaMembership =
                membershipRepository.save(new OrganizationMembership(me, zeta, OrganizationRole.MEMBER));
        OrganizationMembership alphaMembership =
                membershipRepository.save(new OrganizationMembership(me, alpha, OrganizationRole.PRESIDENT));
        membershipRepository.save(new OrganizationMembership(someoneElse, other, OrganizationRole.ORGANIZER));

        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].membershipId").value(alphaMembership.getId()))
                .andExpect(jsonPath("$[0].organizationId").value(alpha.getId()))
                .andExpect(jsonPath("$[0].organizationName").value(alpha.getName()))
                .andExpect(jsonPath("$[0].role").value("PRESIDENT"))
                .andExpect(jsonPath("$[0].joinedAt").exists())
                .andExpect(jsonPath("$[1].membershipId").value(zetaMembership.getId()))
                .andExpect(jsonPath("$[1].organizationName").value(zeta.getName()))
                .andExpect(jsonPath("$[1].role").value("MEMBER"));
    }
}
