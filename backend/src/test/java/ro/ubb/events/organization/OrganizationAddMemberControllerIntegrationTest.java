package ro.ubb.events.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
 * POST /api/v1/organizations/{orgId}/members end to end: who may add (president / ADMIN),
 * 201 + Location, 400/401/403/404/409, case-insensitive email lookup.
 */
class OrganizationAddMemberControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/organizations/{orgId}/members";
    private static final long UNKNOWN_ORG_ID = 999_999_999L;

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;

    private Organization organization;
    private User president;
    private User organizer;
    private User newcomer;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        president = createUser(uniqueEmail("president"), GlobalRole.STUDENT);
        organizer = createUser(uniqueEmail("organizer"), GlobalRole.STUDENT);
        newcomer = createUser(uniqueEmail("newcomer"), GlobalRole.STUDENT);
        membershipRepository.save(new OrganizationMembership(president, organization, OrganizationRole.PRESIDENT));
        membershipRepository.save(new OrganizationMembership(organizer, organization, OrganizationRole.ORGANIZER));
    }

    @Test
    void presidentAddsMember() throws Exception {
        String body = addMember(organization.getId(), token(president), newcomer.getEmail(), "MEMBER")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(newcomer.getId()))
                .andExpect(jsonPath("$.email").value(newcomer.getEmail()))
                .andExpect(jsonPath("$.fullName").value(newcomer.getFullName()))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.joinedAt").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode created = objectMapper.readTree(body);
        long membershipId = created.get("membershipId").asLong();
        assertThat(membershipRepository.findByUserIdAndOrganizationId(newcomer.getId(), organization.getId()))
                .get().extracting(OrganizationMembership::getId, OrganizationMembership::getOrgRole)
                .containsExactly(membershipId, OrganizationRole.MEMBER);
    }

    @Test
    void createdResponseHasLocationOfTheNewMembership() throws Exception {
        addMember(organization.getId(), token(president), newcomer.getEmail(), "ORGANIZER")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ORGANIZER"))
                .andExpect(header().string(HttpHeaders.LOCATION,
                        endsWith("/api/v1/organizations/" + organization.getId() + "/members/"
                                + membershipIdOf(newcomer))));
    }

    @Test
    void emailLookupIgnoresCase() throws Exception {
        addMember(organization.getId(), token(president), newcomer.getEmail().toUpperCase(), "MEMBER")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(newcomer.getEmail()));
    }

    @Test
    void addingTheSameUserTwiceReturns409() throws Exception {
        String token = token(president);
        addMember(organization.getId(), token, newcomer.getEmail(), "MEMBER").andExpect(status().isCreated());

        addMember(organization.getId(), token, newcomer.getEmail(), "ORGANIZER")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_MEMBER"));
    }

    @Test
    void unknownEmailReturns404UserNotFound() throws Exception {
        addMember(organization.getId(), token(president), uniqueEmail("nobody"), "MEMBER")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void organizerGets403() throws Exception {
        addMember(organization.getId(), token(organizer), newcomer.getEmail(), "MEMBER")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void studentWhoIsNotAMemberGets403() throws Exception {
        addMember(organization.getId(), tokenForNewStudent(), newcomer.getEmail(), "MEMBER")
                .andExpect(status().isForbidden());
    }

    @Test
    void adminWhoIsNotAMemberCanAdd() throws Exception {
        addMember(organization.getId(), adminToken(), newcomer.getEmail(), "MEMBER")
                .andExpect(status().isCreated());
    }

    @Test
    void adminGets404ForUnknownOrganization() throws Exception {
        addMember(UNKNOWN_ORG_ID, adminToken(), newcomer.getEmail(), "MEMBER")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void presidentRoleIsRejectedWith400() throws Exception {
        addMember(organization.getId(), token(president), newcomer.getEmail(), "PRESIDENT")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("role"));
    }

    @Test
    void invalidEmailAndMissingRoleReturn400WithFieldErrors() throws Exception {
        addMember(organization.getId(), token(president), "not-an-email", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'role')]").exists());
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        addMember(organization.getId(), null, newcomer.getEmail(), "MEMBER")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private ResultActions addMember(long orgId, String token, String email, String role) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("role", role);
        var request = post(URL, orgId).contentType(MediaType.APPLICATION_JSON).content(json(body));
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, bearer(token));
        }
        return mockMvc.perform(request);
    }

    private String token(User user) throws Exception {
        return loginAndGetToken(user.getEmail());
    }

    private String adminToken() throws Exception {
        String email = uniqueEmail("admin");
        createUser(email, GlobalRole.ADMIN);
        return loginAndGetToken(email);
    }

    private long membershipIdOf(User user) {
        return membershipRepository.findByUserIdAndOrganizationId(user.getId(), organization.getId())
                .orElseThrow().getId();
    }
}
