package ro.ubb.events.organizing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.ResultActions;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.repository.EventRepository;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.organizing.entity.EventTask;
import ro.ubb.events.organizing.repository.EventTaskRepository;
import ro.ubb.events.support.IntegrationTestBase;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/** GET/POST /api/v1/events/{eventId}/tasks end to end. */
class TaskControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/events/{eventId}/tasks";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private EventTaskRepository taskRepository;

    private Organization organization;
    private User organizer;
    private User member;
    private OrganizationMembership organizerMembership;
    private Event event;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        organizer = createUser(uniqueEmail("organizer"), GlobalRole.STUDENT);
        organizerMembership = membershipRepository.save(
                new OrganizationMembership(organizer, organization, OrganizationRole.ORGANIZER));
        member = memberOf(organization, OrganizationRole.MEMBER);
        Instant start = Instant.now().plus(Duration.ofDays(10));
        event = eventRepository.save(new Event(organization, organizer, "Workshop", "FSEGA",
                EventCategory.WORKSHOP, start, start.plus(Duration.ofHours(2)), 30));
    }

    // ---- GET

    @Test
    void listWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get(URL, event.getId())).andExpect(status().isUnauthorized());
    }

    @Test
    void organizerListsTasksByDueDateWithTasksWithoutDueDateLast() throws Exception {
        Instant soon = Instant.now().plus(Duration.ofDays(2)).truncatedTo(ChronoUnit.SECONDS);
        Instant later = Instant.now().plus(Duration.ofDays(5)).truncatedTo(ChronoUnit.SECONDS);
        saveTask("No due date", null, null);
        saveTask("Later", later, null);
        saveTask("Soon", soon, organizerMembership);

        mockMvc.perform(get(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("Soon"))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].dueAt").value(soon.toString()))
                .andExpect(jsonPath("$[0].assignee.membershipId").value(organizerMembership.getId()))
                .andExpect(jsonPath("$[0].assignee.fullName").value(organizer.getFullName()))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1].title").value("Later"))
                .andExpect(jsonPath("$[1].assignee").value((Object) null))
                .andExpect(jsonPath("$[2].title").value("No due date"));
    }

    @Test
    void plainMemberCannotList() throws Exception {
        mockMvc.perform(get(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token(member))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void listOfUnknownEventReturns404() throws Exception {
        mockMvc.perform(get(URL, 999_999_999L).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isNotFound());
    }

    // ---- POST

    @Test
    void organizerCreatesATaskWithAnAssignee() throws Exception {
        Instant dueAt = Instant.now().plus(Duration.ofDays(3)).truncatedTo(ChronoUnit.SECONDS);
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Order snacks");
        body.put("description", "For 30 people");
        body.put("dueAt", dueAt.toString());
        body.put("assigneeMembershipId", organizerMembership.getId());

        postTask(event.getId(), token(organizer), body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Order snacks"))
                .andExpect(jsonPath("$.description").value("For 30 people"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.dueAt").value(dueAt.toString()))
                .andExpect(jsonPath("$.assignee.membershipId").value(organizerMembership.getId()))
                .andExpect(jsonPath("$.assignee.fullName").value(organizer.getFullName()));

        assertThat(taskRepository.findByEventIdOrderByDueAtAsc(event.getId())).hasSize(1);
    }

    @Test
    void organizerCreatesATaskWithOnlyATitle() throws Exception {
        postTask(event.getId(), token(organizer), Map.of("title", "Print posters"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.assignee").value((Object) null))
                .andExpect(jsonPath("$.dueAt").value((Object) null));
    }

    @Test
    void assigneeFromAnotherOrganizationReturns400() throws Exception {
        Organization other = organizationRepository.save(new Organization("Other " + UUID.randomUUID(), null, null));
        User foreign = memberOf(other, OrganizationRole.ORGANIZER);
        Long foreignMembershipId = membershipRepository
                .findByUserIdAndOrganizationId(foreign.getId(), other.getId()).orElseThrow().getId();

        postTask(event.getId(), token(organizer), Map.of("title", "t", "assigneeMembershipId", foreignMembershipId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASSIGNEE_NOT_IN_ORGANIZATION"));
        assertThat(taskRepository.findByEventIdOrderByDueAtAsc(event.getId())).isEmpty();
    }

    @Test
    void unknownAssigneeMembershipReturns400() throws Exception {
        postTask(event.getId(), token(organizer), Map.of("title", "t", "assigneeMembershipId", 999_999_999L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASSIGNEE_NOT_IN_ORGANIZATION"));
    }

    @Test
    void plainMemberCannotCreate() throws Exception {
        postTask(event.getId(), token(member), Map.of("title", "t"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void blankOrTooLongTitleReturns400() throws Exception {
        postTask(event.getId(), token(organizer), Map.of("title", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
        postTask(event.getId(), token(organizer), Map.of("title", "x".repeat(201)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
    }

    @Test
    void createOnUnknownEventReturns404() throws Exception {
        postTask(999_999_999L, token(organizer), Map.of("title", "t")).andExpect(status().isNotFound());
    }

    // ---- helpers

    private ResultActions postTask(Long eventId, String token, Object body) throws Exception {
        return mockMvc.perform(post(URL, eventId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)));
    }

    private void saveTask(String title, Instant dueAt, OrganizationMembership assignee) {
        EventTask task = new EventTask(event, title);
        task.setDueAt(dueAt);
        task.setAssignee(assignee);
        taskRepository.save(task);
    }

    private User memberOf(Organization org, OrganizationRole role) {
        User user = createUser(uniqueEmail(role.name().toLowerCase()), GlobalRole.STUDENT);
        membershipRepository.save(new OrganizationMembership(user, org, role));
        return user;
    }

    private String token(User user) throws Exception {
        return loginAndGetToken(user.getEmail());
    }
}
