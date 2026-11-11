package ro.ubb.events.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * POST /api/v1/organizations/{orgId}/events end to end: who may create (organizer / president of
 * that organization, not ADMIN), 201 + Location, DRAFT status, validation errors on the right fields.
 */
class EventControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/organizations/{orgId}/events";
    private static final Instant START = Instant.now().plus(Duration.ofDays(30)).truncatedTo(ChronoUnit.SECONDS);
    private static final Instant END = START.plus(Duration.ofHours(2));
    private static final Instant DEADLINE = START.minus(Duration.ofDays(1));

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Organization organization;
    private User president;
    private User organizer;
    private User member;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        president = memberWithRole(organization, OrganizationRole.PRESIDENT);
        organizer = memberWithRole(organization, OrganizationRole.ORGANIZER);
        member = memberWithRole(organization, OrganizationRole.MEMBER);
    }

    @Test
    void organizerCreatesADraftEvent() throws Exception {
        String body = create(organization.getId(), token(organizer), validBody())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.organizationId").value(organization.getId()))
                .andExpect(jsonPath("$.organizationName").value(organization.getName()))
                .andExpect(jsonPath("$.title").value("Intro to Docker"))
                .andExpect(jsonPath("$.description").value("Hands-on workshop"))
                .andExpect(jsonPath("$.location").value("FSEGA, room 2"))
                .andExpect(jsonPath("$.category").value("WORKSHOP"))
                .andExpect(jsonPath("$.startAt").value(START.toString()))
                .andExpect(jsonPath("$.endAt").value(END.toString()))
                .andExpect(jsonPath("$.capacity").value(40))
                .andExpect(jsonPath("$.registrationDeadline").value(DEADLINE.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.confirmedCount").value(0))
                .andExpect(jsonPath("$.freePlaces").value(40))
                .andExpect(jsonPath("$.version").value(0))
                .andReturn().getResponse().getContentAsString();

        long eventId = objectMapper.readTree(body).get("id").asLong();
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT organization_id, created_by, status FROM events WHERE id = ?", eventId);
        assertThat(row).containsEntry("organization_id", organization.getId())
                .containsEntry("created_by", organizer.getId())
                .containsEntry("status", "DRAFT");
    }

    @Test
    void createdResponseHasLocationOfTheEvent() throws Exception {
        MockHttpServletResponse response = create(organization.getId(), token(organizer), validBody())
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        long eventId = objectMapper.readTree(response.getContentAsString()).get("id").asLong();

        assertThat(response.getHeader(HttpHeaders.LOCATION)).endsWith("/api/v1/events/" + eventId);
    }

    @Test
    void presidentCanCreate() throws Exception {
        create(organization.getId(), token(president), validBody()).andExpect(status().isCreated());
    }

    @Test
    void deadlineIsOptional() throws Exception {
        Map<String, Object> body = validBody();
        body.remove("registrationDeadline");
        create(organization.getId(), token(organizer), body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationDeadline").value(nullValue()));
    }

    @Test
    void memberGets403() throws Exception {
        create(organization.getId(), token(member), validBody())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void organizerOfAnotherOrganizationGets403() throws Exception {
        Organization other = organizationRepository.save(new Organization("Other " + UUID.randomUUID(), null, null));

        create(other.getId(), token(organizer), validBody()).andExpect(status().isForbidden());
    }

    @Test
    void adminWithoutMembershipGets403() throws Exception {
        String email = uniqueEmail("admin");
        createUser(email, GlobalRole.ADMIN);

        create(organization.getId(), loginAndGetToken(email), validBody()).andExpect(status().isForbidden());
    }

    @Test
    void endBeforeStartReturns400OnEndAt() throws Exception {
        Map<String, Object> body = validBody();
        body.put("endAt", START.minus(Duration.ofHours(1)).toString());

        create(organization.getId(), token(organizer), body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.length()").value(1))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("endAt"));
    }

    @Test
    void zeroCapacityAndDeadlineAfterStartReturn400WithBothFields() throws Exception {
        Map<String, Object> body = validBody();
        body.put("capacity", 0);
        body.put("registrationDeadline", START.plus(Duration.ofHours(1)).toString());

        create(organization.getId(), token(organizer), body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'capacity')]").exists())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'registrationDeadline')]").exists());
    }

    @Test
    void startInThePastReturns400OnStartAt() throws Exception {
        Map<String, Object> body = validBody();
        Instant past = Instant.now().minus(Duration.ofDays(1));
        body.put("startAt", past.toString());
        body.put("endAt", past.plus(Duration.ofHours(2)).toString());
        body.remove("registrationDeadline");

        create(organization.getId(), token(organizer), body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("startAt"));
    }

    @Test
    void unknownCategoryReturns400() throws Exception {
        Map<String, Object> body = validBody();
        body.put("category", "PARTY");

        create(organization.getId(), token(organizer), body).andExpect(status().isBadRequest());
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mockMvc.perform(post(URL, organization.getId()).contentType(MediaType.APPLICATION_JSON).content(json(validBody())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private ResultActions create(long orgId, String token, Map<String, Object> body) throws Exception {
        return mockMvc.perform(post(URL, orgId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)));
    }

    private static Map<String, Object> validBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Intro to Docker");
        body.put("description", "Hands-on workshop");
        body.put("location", "FSEGA, room 2");
        body.put("category", "WORKSHOP");
        body.put("startAt", START.toString());
        body.put("endAt", END.toString());
        body.put("capacity", 40);
        body.put("registrationDeadline", DEADLINE.toString());
        return body;
    }

    private User memberWithRole(Organization org, OrganizationRole role) {
        User user = createUser(uniqueEmail(role.name().toLowerCase()), GlobalRole.STUDENT);
        membershipRepository.save(new OrganizationMembership(user, org, role));
        return user;
    }

    private String token(User user) throws Exception {
        return loginAndGetToken(user.getEmail());
    }
}
