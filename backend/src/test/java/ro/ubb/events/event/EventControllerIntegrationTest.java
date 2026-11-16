package ro.ubb.events.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void organizerCanPublishDraftAndMemberCannotChangeStatus() throws Exception {
        long eventId = createEvent();
        mockMvc.perform(post("/api/v1/events/{id}/status", eventId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(post("/api/v1/events/{id}/status", eventId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(member)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void invalidStatusTransitionReturns409WithCode() throws Exception {
        long eventId = createEvent();
        mockMvc.perform(post("/api/v1/events/{id}/status", eventId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ONGOING\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("DRAFT to ONGOING")));
    }

    @Test
    void unknownTargetStatusReturns400() throws Exception {
        long eventId = createEvent();
        mockMvc.perform(post("/api/v1/events/{id}/status", eventId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest());
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

    @Test
    void organizerCanEditDraftAndPublishedButNotCompleted() throws Exception {
        long draftId = createEvent();
        Map<String, Object> edit = updateBody(0);
        mockMvc.perform(put("/api/v1/events/{id}", draftId).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content(json(edit)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated event"))
                .andExpect(jsonPath("$.version").value(1));

        long publishedId = createEvent();
        jdbcTemplate.update("UPDATE events SET status = 'PUBLISHED' WHERE id = ?", publishedId);
        Map<String, Object> publishedEdit = updateBody(0);
        mockMvc.perform(put("/api/v1/events/{id}", publishedId).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content(json(publishedEdit)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));

        long completedId = createEvent();
        jdbcTemplate.update("UPDATE events SET status = 'COMPLETED' WHERE id = ?", completedId);
        mockMvc.perform(put("/api/v1/events/{id}", completedId).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content(json(updateBody(0))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("EVENT_NOT_EDITABLE"));
    }

    @Test
    void staleVersionAndCapacityBelowConfirmedCountAreRejected() throws Exception {
        long id = createEvent();
        mockMvc.perform(put("/api/v1/events/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content(json(updateBody(0))))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/events/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content(json(updateBody(0))))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONCURRENT_UPDATE"));

        long registeredEvent = createEvent();
        jdbcTemplate.update("INSERT INTO registrations (event_id, user_id, status, registered_at) VALUES (?, ?, 'CONFIRMED', now())",
                registeredEvent, organizer.getId());
        jdbcTemplate.update("INSERT INTO registrations (event_id, user_id, status, registered_at) VALUES (?, ?, 'CONFIRMED', now())",
                registeredEvent, president.getId());
        Map<String, Object> tooSmall = updateBody(0);
        tooSmall.put("capacity", 1);
        mockMvc.perform(put("/api/v1/events/{id}", registeredEvent).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content(json(tooSmall)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors[0].field").value("capacity"));
    }

    private long createEvent() throws Exception {
        String body = create(organization.getId(), token(organizer), validBody()).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private static Map<String, Object> updateBody(long version) {
        Map<String, Object> body = validBody();
        body.put("title", "Updated event");
        body.put("version", version);
        return body;
    }

    @Test
    void nonOrganizerCannotEditAndUnknownEventIs404() throws Exception {
        long id = createEvent();
        mockMvc.perform(put("/api/v1/events/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(token(member)))
                        .contentType(MediaType.APPLICATION_JSON).content(json(updateBody(0))))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/events/{id}", 999999L).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content(json(updateBody(0))))
                .andExpect(status().isNotFound());
    }

    @Test
    void draftIsVisibleToOrganizerAndHiddenFromOtherUsers() throws Exception {
        long id = createEvent();
        mockMvc.perform(get("/api/v1/events/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
        mockMvc.perform(get("/api/v1/events/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(member))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void eventDetailsIncludeConfirmedCountAndReturnCancelledAndCompletedEvents() throws Exception {
        long id = createEvent();
        jdbcTemplate.update("UPDATE events SET capacity = 30 WHERE id = ?", id);
        jdbcTemplate.update("UPDATE events SET status = 'CANCELLED' WHERE id = ?", id);
        jdbcTemplate.update("INSERT INTO registrations (event_id, user_id, status, registered_at) VALUES (?, ?, 'CONFIRMED', now())",
                id, organizer.getId());
        jdbcTemplate.update("INSERT INTO registrations (event_id, user_id, status, registered_at) VALUES (?, ?, 'CONFIRMED', now())",
                id, president.getId());

        mockMvc.perform(get("/api/v1/events/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedCount").value(2))
                .andExpect(jsonPath("$.freePlaces").value(28))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        jdbcTemplate.update("UPDATE events SET status = 'COMPLETED' WHERE id = ?", id);
        mockMvc.perform(get("/api/v1/events/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void eventDetailsReturn404ForUnknownIdAnd401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/events/{id}", 999999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/events/{id}", 999999L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void organizationEventsShowDraftsOnlyToOrganizersAndUnknownOrganizationReturns404() throws Exception {
        long draftId = createEvent();
        Map<String, Object> laterBody = validBody();
        laterBody.put("title", "Published event");
        laterBody.put("startAt", START.plus(Duration.ofDays(1)).toString());
        laterBody.put("endAt", END.plus(Duration.ofDays(1)).toString());
        long publishedId = objectMapper.readTree(create(organization.getId(), token(organizer), laterBody)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(post("/api/v1/events/{id}/status", publishedId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(organizer)))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/organizations/{orgId}/events", organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(draftId))
                .andExpect(jsonPath("$[0].status").value("DRAFT"))
                .andExpect(jsonPath("$[1].id").value(publishedId))
                .andExpect(jsonPath("$[1].status").value("PUBLISHED"));

        mockMvc.perform(get("/api/v1/organizations/{orgId}/events", organization.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(member))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(publishedId))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/v1/organizations/{orgId}/events", 999999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(member))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
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
