package ro.ubb.events.organizing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
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
import ro.ubb.events.organizing.entity.TaskStatus;
import ro.ubb.events.organizing.repository.EventTaskRepository;
import ro.ubb.events.support.IntegrationTestBase;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/** PATCH /api/v1/events/{eventId}/tasks/{taskId} end to end: status, assignee, and who may change them. */
class TaskUpdateControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/events/{eventId}/tasks/{taskId}";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private EventTaskRepository taskRepository;

    private Organization organization;
    private User organizer;
    private User member;
    private OrganizationMembership organizerMembership;
    private OrganizationMembership memberMembership;
    private Event event;
    private EventTask task;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        organizer = createUser(uniqueEmail("organizer"), GlobalRole.STUDENT);
        organizerMembership = membershipRepository.save(
                new OrganizationMembership(organizer, organization, OrganizationRole.ORGANIZER));
        member = createUser(uniqueEmail("member"), GlobalRole.STUDENT);
        memberMembership = membershipRepository.save(
                new OrganizationMembership(member, organization, OrganizationRole.MEMBER));
        event = newEvent(organization, organizer);
        task = taskRepository.save(new EventTask(event, "Book the room"));
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mockMvc.perform(patch(URL, event.getId(), task.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changesTheStatusAlone() throws Exception {
        patchTask(event.getId(), task.getId(), token(organizer), Map.of("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.assignee").value((Object) null));
        assertThat(reload().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void changesTheAssigneeAlone() throws Exception {
        patchTask(event.getId(), task.getId(), token(organizer), Map.of("assigneeMembershipId", memberMembership.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.assignee.membershipId").value(memberMembership.getId()))
                .andExpect(jsonPath("$.assignee.fullName").value(member.getFullName()));
    }

    @Test
    void changesStatusAndAssigneeTogetherAndCanGoBackToTodo() throws Exception {
        patchTask(event.getId(), task.getId(), token(organizer),
                Map.of("status", "DONE", "assigneeMembershipId", organizerMembership.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.assignee.membershipId").value(organizerMembership.getId()));

        patchTask(event.getId(), task.getId(), token(organizer), Map.of("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.assignee.membershipId").value(organizerMembership.getId()));
    }

    @Test
    void unassignRemovesTheAssignee() throws Exception {
        task.setAssignee(memberMembership);
        taskRepository.save(task);

        patchTask(event.getId(), task.getId(), token(organizer), Map.of("unassign", true))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee").value((Object) null));
        assertThat(reload().getAssignee()).isNull();
    }

    @Test
    void unassignTogetherWithAnAssigneeReturns400() throws Exception {
        patchTask(event.getId(), task.getId(), token(organizer),
                Map.of("unassign", true, "assigneeMembershipId", memberMembership.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("unassign"));
    }

    @Test
    void anEmptyBodyChangesNothing() throws Exception {
        patchTask(event.getId(), task.getId(), token(organizer), Map.of())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void unknownStatusReturns400() throws Exception {
        patchTask(event.getId(), task.getId(), token(organizer), Map.of("status", "FINISHED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
        assertThat(reload().getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void assigneeFromAnotherOrganizationReturns400AndChangesNothing() throws Exception {
        Organization other = organizationRepository.save(new Organization("Other " + UUID.randomUUID(), null, null));
        User foreign = createUser(uniqueEmail("foreign"), GlobalRole.STUDENT);
        OrganizationMembership foreignMembership = membershipRepository.save(
                new OrganizationMembership(foreign, other, OrganizationRole.ORGANIZER));

        patchTask(event.getId(), task.getId(), token(organizer),
                Map.of("status", "DONE", "assigneeMembershipId", foreignMembership.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASSIGNEE_NOT_IN_ORGANIZATION"));
        EventTask unchanged = reload();
        assertThat(unchanged.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(unchanged.getAssignee()).isNull();
    }

    @Test
    void taskOfAnotherEventReturns404() throws Exception {
        Event otherEvent = newEvent(organization, organizer);

        patchTask(otherEvent.getId(), task.getId(), token(organizer), Map.of("status", "DONE"))
                .andExpect(status().isNotFound());
        assertThat(reload().getStatus()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void unknownTaskAndUnknownEventReturn404() throws Exception {
        patchTask(event.getId(), 999_999_999L, token(organizer), Map.of("status", "DONE"))
                .andExpect(status().isNotFound());
        patchTask(999_999_999L, task.getId(), token(organizer), Map.of("status", "DONE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void plainMemberCannotChangeATask() throws Exception {
        patchTask(event.getId(), task.getId(), token(member), Map.of("status", "DONE"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        assertThat(reload().getStatus()).isEqualTo(TaskStatus.TODO);
    }

    // ---- helpers

    private ResultActions patchTask(Long eventId, Long taskId, String token, Object body) throws Exception {
        return mockMvc.perform(patch(URL, eventId, taskId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)));
    }

    private EventTask reload() {
        return taskRepository.findByEventIdWithAssignee(event.getId()).stream()
                .filter(t -> t.getId().equals(task.getId())).findFirst().orElseThrow();
    }

    private Event newEvent(Organization org, User creator) {
        Instant start = Instant.now().plus(Duration.ofDays(10));
        return eventRepository.save(new Event(org, creator, "Workshop " + UUID.randomUUID(), "FSEGA",
                EventCategory.WORKSHOP, start, start.plus(Duration.ofHours(2)), 30));
    }

    private String token(User user) throws Exception {
        return loginAndGetToken(user.getEmail());
    }
}
