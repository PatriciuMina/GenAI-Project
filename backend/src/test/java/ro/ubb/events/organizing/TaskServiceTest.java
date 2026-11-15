package ro.ubb.events.organizing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.service.MembershipService;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.organizing.dto.TaskRequest;
import ro.ubb.events.organizing.dto.TaskResponse;
import ro.ubb.events.organizing.entity.EventTask;
import ro.ubb.events.organizing.entity.TaskStatus;
import ro.ubb.events.organizing.mapper.TaskMapper;
import ro.ubb.events.organizing.repository.EventTaskRepository;
import ro.ubb.events.organizing.service.TaskService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/** Unit test: repository and other features' services are mocked, the mapper is real. */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final Long EVENT_ID = 5L;
    private static final Long ORG_ID = 2L;
    private static final Long CALLER_ID = 10L;
    private static final Long MEMBERSHIP_ID = 4L;

    @Mock private EventTaskRepository taskRepository;
    @Mock private EventService eventService;
    @Mock private OrgPermissionService orgPermissionService;
    @Mock private MembershipService membershipService;

    private TaskService taskService;
    private Event event;
    private OrganizationMembership assignee;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, eventService, orgPermissionService, membershipService,
                new TaskMapper());
        Organization organization = new Organization("UBB CS Society", null, null);
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        User elena = new User("elena@test.ro", "hash", "Elena Marin", GlobalRole.STUDENT);
        Instant start = Instant.now().plus(Duration.ofDays(10));
        event = new Event(organization, elena, "Workshop", "FSEGA", EventCategory.WORKSHOP,
                start, start.plus(Duration.ofHours(2)), 30);
        assignee = new OrganizationMembership(elena, organization, OrganizationRole.ORGANIZER);
        ReflectionTestUtils.setField(assignee, "id", MEMBERSHIP_ID);
    }

    // ---- list

    @Test
    void organizerListsTheTasksMappedWithTheirAssignee() {
        EventTask assigned = new EventTask(event, "Book the room");
        assigned.setAssignee(assignee);
        EventTask unassigned = new EventTask(event, "Order snacks");
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(true);
        when(taskRepository.findByEventIdWithAssignee(EVENT_ID)).thenReturn(List.of(assigned, unassigned));

        List<TaskResponse> result = taskService.list(EVENT_ID, CALLER_ID);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Book the room");
        assertThat(result.get(0).status()).isEqualTo(TaskStatus.TODO);
        assertThat(result.get(0).assignee().membershipId()).isEqualTo(MEMBERSHIP_ID);
        assertThat(result.get(0).assignee().fullName()).isEqualTo("Elena Marin");
        assertThat(result.get(1).assignee()).isNull();
    }

    @Test
    void nonOrganizerCannotList() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(false);

        assertThatThrownBy(() -> taskService.list(EVENT_ID, CALLER_ID))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(ex.getCode()).isEqualTo("FORBIDDEN");
                });
        verifyNoInteractions(taskRepository);
    }

    @Test
    void listOfUnknownEventThrowsNotFound() {
        when(eventService.getEntityById(EVENT_ID)).thenThrow(new NotFoundException("Event 5 not found"));

        assertThatThrownBy(() -> taskService.list(EVENT_ID, CALLER_ID)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(orgPermissionService, taskRepository);
    }

    // ---- create

    @Test
    void createsATodoTaskWithTheGivenFieldsAndAssignee() {
        Instant dueAt = Instant.parse("2026-11-13T12:00:00Z");
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(true);
        when(membershipService.findEntityInOrganization(ORG_ID, MEMBERSHIP_ID)).thenReturn(Optional.of(assignee));
        when(taskRepository.saveAndFlush(any(EventTask.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse result = taskService.create(EVENT_ID,
                new TaskRequest("  Order snacks ", "For 30 people", dueAt, MEMBERSHIP_ID), CALLER_ID);

        ArgumentCaptor<EventTask> saved = ArgumentCaptor.forClass(EventTask.class);
        verify(taskRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getEvent()).isSameAs(event);
        assertThat(saved.getValue().getTitle()).isEqualTo("Order snacks");
        assertThat(saved.getValue().getDescription()).isEqualTo("For 30 people");
        assertThat(saved.getValue().getDueAt()).isEqualTo(dueAt);
        assertThat(saved.getValue().getAssignee()).isSameAs(assignee);
        assertThat(saved.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.assignee().fullName()).isEqualTo("Elena Marin");
    }

    @Test
    void assigneeDescriptionAndDueDateAreOptional() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(true);
        when(taskRepository.saveAndFlush(any(EventTask.class))).thenAnswer(i -> i.getArgument(0));

        TaskResponse result = taskService.create(EVENT_ID, new TaskRequest("Print posters", null, null, null),
                CALLER_ID);

        assertThat(result.assignee()).isNull();
        assertThat(result.dueAt()).isNull();
        assertThat(result.status()).isEqualTo(TaskStatus.TODO);
        verifyNoInteractions(membershipService);
    }

    @Test
    void assigneeFromAnotherOrganizationOrUnknownMembershipIsRejected() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(true);
        when(membershipService.findEntityInOrganization(ORG_ID, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(EVENT_ID, new TaskRequest("t", null, null, 99L), CALLER_ID))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getCode()).isEqualTo("ASSIGNEE_NOT_IN_ORGANIZATION");
                });
        verify(taskRepository, never()).saveAndFlush(any());
    }

    @Test
    void nonOrganizerCannotCreateAndTheAssigneeIsNotLookedUp() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(false);

        assertThatThrownBy(() -> taskService.create(EVENT_ID, new TaskRequest("t", null, null, MEMBERSHIP_ID),
                CALLER_ID))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
        verifyNoInteractions(membershipService, taskRepository);
    }

    @Test
    void createOnUnknownEventThrowsNotFound() {
        when(eventService.getEntityById(EVENT_ID)).thenThrow(new NotFoundException("Event 5 not found"));

        assertThatThrownBy(() -> taskService.create(EVENT_ID, new TaskRequest("t", null, null, null), CALLER_ID))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(orgPermissionService, membershipService, taskRepository);
    }
}
