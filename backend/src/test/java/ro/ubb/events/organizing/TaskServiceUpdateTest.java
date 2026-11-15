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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.common.exception.ValidationException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.service.MembershipService;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.organizing.dto.TaskResponse;
import ro.ubb.events.organizing.dto.TaskUpdateRequest;
import ro.ubb.events.organizing.entity.EventTask;
import ro.ubb.events.organizing.entity.TaskStatus;
import ro.ubb.events.organizing.mapper.TaskMapper;
import ro.ubb.events.organizing.repository.EventTaskRepository;
import ro.ubb.events.organizing.service.TaskService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/** Unit test of changing a task's status and assignee: repository and other services mocked, mapper real. */
@ExtendWith(MockitoExtension.class)
class TaskServiceUpdateTest {

    private static final Long EVENT_ID = 5L;
    private static final Long TASK_ID = 8L;
    private static final Long ORG_ID = 2L;
    private static final Long CALLER_ID = 10L;
    private static final Long MEMBERSHIP_ID = 4L;

    @Mock private EventTaskRepository taskRepository;
    @Mock private EventService eventService;
    @Mock private OrgPermissionService orgPermissionService;
    @Mock private MembershipService membershipService;

    private TaskService taskService;
    private Event event;
    private EventTask task;
    private OrganizationMembership membership;
    private OrganizationMembership currentAssignee;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, eventService, orgPermissionService, membershipService,
                new TaskMapper());
        Organization organization = new Organization("UBB CS Society", null, null);
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        User elena = new User("elena@test.ro", "hash", "Elena Marin", GlobalRole.STUDENT);
        User ana = new User("ana@test.ro", "hash", "Ana Popescu", GlobalRole.STUDENT);
        Instant start = Instant.now().plus(Duration.ofDays(10));
        event = new Event(organization, elena, "Workshop", "FSEGA", EventCategory.WORKSHOP,
                start, start.plus(Duration.ofHours(2)), 30);
        membership = new OrganizationMembership(elena, organization, OrganizationRole.ORGANIZER);
        ReflectionTestUtils.setField(membership, "id", MEMBERSHIP_ID);
        currentAssignee = new OrganizationMembership(ana, organization, OrganizationRole.PRESIDENT);
        ReflectionTestUtils.setField(currentAssignee, "id", 1L);
        task = new EventTask(event, "Book the room");
        ReflectionTestUtils.setField(task, "id", TASK_ID);
    }

    private void organizerOfAnExistingTask() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(true);
        when(taskRepository.findByIdAndEventId(TASK_ID, EVENT_ID)).thenReturn(Optional.of(task));
    }

    private void savingReturnsTheTask() {
        when(taskRepository.saveAndFlush(any(EventTask.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void changesOnlyTheStatusAndLeavesTheAssignee() {
        task.setAssignee(currentAssignee);
        organizerOfAnExistingTask();
        savingReturnsTheTask();

        TaskResponse result = taskService.update(EVENT_ID, TASK_ID, new TaskUpdateRequest(TaskStatus.IN_PROGRESS,
                null, null), CALLER_ID);

        assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.assignee().membershipId()).isEqualTo(1L);
        verifyNoInteractions(membershipService);
    }

    @Test
    void changesOnlyTheAssigneeAndLeavesTheStatus() {
        task.setStatus(TaskStatus.IN_PROGRESS);
        organizerOfAnExistingTask();
        savingReturnsTheTask();
        when(membershipService.findEntityInOrganization(ORG_ID, MEMBERSHIP_ID)).thenReturn(Optional.of(membership));

        TaskResponse result = taskService.update(EVENT_ID, TASK_ID, new TaskUpdateRequest(null, MEMBERSHIP_ID, null),
                CALLER_ID);

        assertThat(result.assignee().membershipId()).isEqualTo(MEMBERSHIP_ID);
        assertThat(result.assignee().fullName()).isEqualTo("Elena Marin");
        assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void changesStatusAndAssigneeTogether() {
        organizerOfAnExistingTask();
        savingReturnsTheTask();
        when(membershipService.findEntityInOrganization(ORG_ID, MEMBERSHIP_ID)).thenReturn(Optional.of(membership));

        TaskResponse result = taskService.update(EVENT_ID, TASK_ID,
                new TaskUpdateRequest(TaskStatus.DONE, MEMBERSHIP_ID, null), CALLER_ID);

        assertThat(result.status()).isEqualTo(TaskStatus.DONE);
        assertThat(result.assignee().membershipId()).isEqualTo(MEMBERSHIP_ID);
    }

    @Test
    void aTaskCanGoBackToTodo() {
        task.setStatus(TaskStatus.DONE);
        organizerOfAnExistingTask();
        savingReturnsTheTask();

        assertThat(taskService.update(EVENT_ID, TASK_ID, new TaskUpdateRequest(TaskStatus.TODO, null, null),
                CALLER_ID).status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void unassignRemovesTheAssignee() {
        task.setAssignee(currentAssignee);
        organizerOfAnExistingTask();
        savingReturnsTheTask();

        TaskResponse result = taskService.update(EVENT_ID, TASK_ID, new TaskUpdateRequest(null, null, true), CALLER_ID);

        assertThat(result.assignee()).isNull();
    }

    @Test
    void unassignTogetherWithAnAssigneeIsRejectedAndNothingIsSaved() {
        organizerOfAnExistingTask();

        assertThatThrownBy(() -> taskService.update(EVENT_ID, TASK_ID,
                new TaskUpdateRequest(null, MEMBERSHIP_ID, true), CALLER_ID))
                .isInstanceOfSatisfying(ValidationException.class,
                        ex -> assertThat(ex.getFieldErrors()).extracting("field").containsExactly("unassign"));
        verify(taskRepository, never()).saveAndFlush(any());
    }

    @Test
    void unassignFalseIsTheSameAsNotSendingIt() {
        task.setAssignee(currentAssignee);
        organizerOfAnExistingTask();
        savingReturnsTheTask();

        TaskResponse result = taskService.update(EVENT_ID, TASK_ID, new TaskUpdateRequest(null, null, false),
                CALLER_ID);

        assertThat(result.assignee().membershipId()).isEqualTo(1L);
    }

    @Test
    void anEmptyRequestChangesNothing() {
        task.setAssignee(currentAssignee);
        task.setStatus(TaskStatus.IN_PROGRESS);
        organizerOfAnExistingTask();
        savingReturnsTheTask();

        TaskResponse result = taskService.update(EVENT_ID, TASK_ID, new TaskUpdateRequest(null, null, null), CALLER_ID);

        assertThat(result.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.assignee().membershipId()).isEqualTo(1L);
    }

    @Test
    void assigneeOutsideTheOrganizationIsRejectedAndTheStatusIsNotApplied() {
        organizerOfAnExistingTask();
        when(membershipService.findEntityInOrganization(ORG_ID, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(EVENT_ID, TASK_ID,
                new TaskUpdateRequest(TaskStatus.DONE, 99L, null), CALLER_ID))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getCode()).isEqualTo("ASSIGNEE_NOT_IN_ORGANIZATION");
                });
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
        verify(taskRepository, never()).saveAndFlush(any());
    }

    @Test
    void taskOfAnotherEventIsNotFound() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(true);
        when(taskRepository.findByIdAndEventId(TASK_ID, EVENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(EVENT_ID, TASK_ID,
                new TaskUpdateRequest(TaskStatus.DONE, null, null), CALLER_ID))
                .isInstanceOf(NotFoundException.class);
        verify(taskRepository, never()).saveAndFlush(any());
    }

    @Test
    void nonOrganizerIsForbiddenAndTheTaskIsNotLoaded() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(false);

        assertThatThrownBy(() -> taskService.update(EVENT_ID, TASK_ID,
                new TaskUpdateRequest(TaskStatus.DONE, null, null), CALLER_ID))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
        verifyNoInteractions(taskRepository, membershipService);
    }

    @Test
    void unknownEventIsNotFound() {
        when(eventService.getEntityById(EVENT_ID)).thenThrow(new NotFoundException("Event 5 not found"));

        assertThatThrownBy(() -> taskService.update(EVENT_ID, TASK_ID,
                new TaskUpdateRequest(TaskStatus.DONE, null, null), CALLER_ID))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(orgPermissionService, taskRepository, membershipService);
    }
}
