package ro.ubb.events.organization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.support.IntegrationTestBase;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/**
 * Proves that {@code @orgPermissions} works inside {@code @PreAuthorize} with the real JWT
 * principal, and that a denied check returns 403 with the common error body.
 * Uses a small controller that exists only in this test.
 */
@Import(OrgPermissionPreAuthorizeIntegrationTest.ProbeController.class)
class OrgPermissionPreAuthorizeIntegrationTest extends IntegrationTestBase {

    @RestController
    static class ProbeController {

        @GetMapping("/test-only/orgs/{orgId}/organizer-area")
        @PreAuthorize("@orgPermissions.isOrganizer(principal.id, #orgId)")
        public String organizerArea(@PathVariable Long orgId) {
            return "ok";
        }

        @GetMapping("/test-only/orgs/{orgId}/admin-or-president")
        @PreAuthorize("hasRole('ADMIN') or @orgPermissions.isPresident(principal.id, #orgId)")
        public String adminOrPresident(@PathVariable Long orgId) {
            return "ok";
        }
    }

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;

    private Organization organization;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
    }

    private String tokenForMember(OrganizationRole role) throws Exception {
        User user = createUser(uniqueEmail(role.name().toLowerCase()), GlobalRole.STUDENT);
        membershipRepository.save(new OrganizationMembership(user, organization, role));
        return loginAndGetToken(user.getEmail());
    }

    @Test
    void organizerIsAllowed() throws Exception {
        mockMvc.perform(get("/test-only/orgs/{id}/organizer-area", organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenForMember(OrganizationRole.ORGANIZER))))
                .andExpect(status().isOk());
    }

    @Test
    void plainMemberIsForbiddenWithCommonErrorBody() throws Exception {
        mockMvc.perform(get("/test-only/orgs/{id}/organizer-area", organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenForMember(OrganizationRole.MEMBER))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void nonMemberIsForbidden() throws Exception {
        mockMvc.perform(get("/test-only/orgs/{id}/organizer-area", organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenForNewStudent())))
                .andExpect(status().isForbidden());
    }

    @Test
    void globalAdminIsAllowedOnlyWhereTheExpressionSaysSo() throws Exception {
        String adminEmail = uniqueEmail("admin");
        createUser(adminEmail, GlobalRole.ADMIN);
        String adminToken = loginAndGetToken(adminEmail);

        mockMvc.perform(get("/test-only/orgs/{id}/admin-or-president", organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/test-only/orgs/{id}/organizer-area", organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isForbidden());
    }
}
