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
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.support.IntegrationTestBase;

/** GET /api/v1/organizations[/{id}] end to end: security, JSON shape, 404. */
class OrganizationControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization organization;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        organization = organizationRepository.save(
                new Organization("Org " + UUID.randomUUID(), "A test organization", "org@test.ro"));
        token = tokenForNewStudent();
    }

    @Test
    void listWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/organizations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void listWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/organizations").header(HttpHeaders.AUTHORIZATION, bearer("not-a-jwt")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void listWithTokenReturnsOrganizations() throws Exception {
        mockMvc.perform(get("/api/v1/organizations").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == %d)].name", organization.getId()).value(organization.getName()));
    }

    @Test
    void getByIdReturnsTheOrganization() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{id}", organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(organization.getId()))
                .andExpect(jsonPath("$.name").value(organization.getName()))
                .andExpect(jsonPath("$.description").value("A test organization"))
                .andExpect(jsonPath("$.contactEmail").value("org@test.ro"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getByUnknownIdReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{id}", 999_999_999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getByIdWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/organizations/{id}", organization.getId()))
                .andExpect(status().isUnauthorized());
    }
}
