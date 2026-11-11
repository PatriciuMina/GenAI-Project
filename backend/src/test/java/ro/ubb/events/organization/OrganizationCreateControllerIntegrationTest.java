package ro.ubb.events.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
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
 * POST /api/v1/organizations end to end: ADMIN only, 201 + Location, the president's membership is
 * created too, duplicate names (ignoring case), unknown president, validation, 401/403.
 */
class OrganizationCreateControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/organizations";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;

    private String adminToken;
    private User futurePresident;
    private String name;

    @BeforeEach
    void setUp() throws Exception {
        String adminEmail = uniqueEmail("admin");
        createUser(adminEmail, GlobalRole.ADMIN);
        adminToken = loginAndGetToken(adminEmail);
        futurePresident = createUser(uniqueEmail("president"), GlobalRole.STUDENT);
        name = "Robotics " + UUID.randomUUID();
    }

    @Test
    void adminCreatesOrganizationWithItsPresident() throws Exception {
        MockHttpServletResponse response = create(adminToken, body(name, futurePresident.getEmail()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value("Robots and coffee"))
                .andExpect(jsonPath("$.contactEmail").value("robotics@ubbcluj.ro"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn().getResponse();
        long id = objectMapper.readTree(response.getContentAsString()).get("id").asLong();

        assertThat(response.getHeader(HttpHeaders.LOCATION)).endsWith("/api/v1/organizations/" + id);
        assertThat(membershipRepository.findByUserIdAndOrganizationId(futurePresident.getId(), id))
                .get().extracting(OrganizationMembership::getOrgRole).isEqualTo(OrganizationRole.PRESIDENT);
    }

    @Test
    void newOrganizationIsListedAndThePresidentSeesItInTheirMemberships() throws Exception {
        create(adminToken, body(name, futurePresident.getEmail())).andExpect(status().isCreated());

        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == '%s')]", name).exists());
        mockMvc.perform(get("/api/v1/users/me/memberships")
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(futurePresident.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].organizationName").value(name))
                .andExpect(jsonPath("$[0].role").value("PRESIDENT"));
    }

    @Test
    void duplicateNameInAnotherCaseReturns409AndCreatesNothing() throws Exception {
        organizationRepository.save(new Organization(name, null, null));
        long countBefore = organizationRepository.count();

        create(adminToken, body(name.toUpperCase(), futurePresident.getEmail()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORGANIZATION_NAME_TAKEN"));

        assertThat(organizationRepository.count()).isEqualTo(countBefore);
        assertThat(membershipRepository.findByUserId(futurePresident.getId())).isEmpty();
    }

    @Test
    void unknownPresidentEmailReturns404AndCreatesNothing() throws Exception {
        create(adminToken, body(name, uniqueEmail("nobody")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));

        assertThat(organizationRepository.existsByNameIgnoreCase(name)).isFalse();
    }

    @Test
    void studentGets403() throws Exception {
        create(tokenForNewStudent(), body(name, futurePresident.getEmail()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        assertThat(organizationRepository.existsByNameIgnoreCase(name)).isFalse();
    }

    @Test
    void presidentOfAnotherOrganizationGets403() throws Exception {
        Organization other = organizationRepository.save(new Organization("Other " + UUID.randomUUID(), null, null));
        User otherPresident = createUser(uniqueEmail("other-president"), GlobalRole.STUDENT);
        membershipRepository.save(new OrganizationMembership(otherPresident, other, OrganizationRole.PRESIDENT));

        create(loginAndGetToken(otherPresident.getEmail()), body(name, futurePresident.getEmail()))
                .andExpect(status().isForbidden());
    }

    @Test
    void optionalFieldsCanBeOmitted() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("presidentEmail", futurePresident.getEmail());

        create(adminToken, body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(nullValue()))
                .andExpect(jsonPath("$.contactEmail").value(nullValue()));
    }

    @Test
    void blankNameAndInvalidEmailsReturn400WithFieldErrors() throws Exception {
        Map<String, Object> body = body(" ", "not-an-email");
        body.put("contactEmail", "also-not-an-email");

        create(adminToken, body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'name')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'presidentEmail')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'contactEmail')]").exists());
    }

    @Test
    void nameLongerThan150CharactersReturns400() throws Exception {
        create(adminToken, body("x".repeat(151), futurePresident.getEmail()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .content(json(body(name, futurePresident.getEmail()))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private ResultActions create(String token, Map<String, Object> body) throws Exception {
        return mockMvc.perform(post(URL)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)));
    }

    private static Map<String, Object> body(String name, String presidentEmail) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", "Robots and coffee");
        body.put("contactEmail", "robotics@ubbcluj.ro");
        body.put("presidentEmail", presidentEmail);
        return body;
    }
}
