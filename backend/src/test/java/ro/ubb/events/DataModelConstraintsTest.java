package ro.ubb.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
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
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.repository.RegistrationRepository;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.repository.UserRepository;

/**
 * Checks that the database (Flyway V1 on a real PostgreSQL 16) enforces the business rules
 * documented in docs/data-model.md. Each test runs in a rolled-back transaction.
 * The dev seed is NOT loaded here (test profile uses only classpath:db/migration).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Testcontainers
class DataModelConstraintsTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired private UserRepository users;
    @Autowired private OrganizationRepository organizations;
    @Autowired private OrganizationMembershipRepository memberships;
    @Autowired private EventRepository events;
    @Autowired private RegistrationRepository registrations;
    @Autowired private EventTaskRepository tasks;
    @Autowired private EntityManager entityManager;

    private User organizer;
    private User student;
    private Organization organization;

    @BeforeEach
    void setUp() {
        organizer = users.save(new User("org@test.ro", "hash", "Organizer", GlobalRole.STUDENT));
        student = users.save(new User("student@test.ro", "hash", "Student", GlobalRole.STUDENT));
        organization = organizations.save(new Organization("Test Org", null, null));
    }

    private Event newEvent(int capacity, Instant start, Instant end) {
        return new Event(organization, organizer, "Event", "Room 1", EventCategory.OTHER, start, end, capacity);
    }

    private Event savedEvent() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        return events.saveAndFlush(newEvent(10, start, start.plus(2, ChronoUnit.HOURS)));
    }

    @Test
    void validGraphIsPersisted() {
        Event event = savedEvent();
        registrations.saveAndFlush(new Registration(event, student));

        assertThat(event.getId()).isNotNull();
        assertThat(event.getVersion()).isZero();
        assertThat(registrations.countByEventIdAndStatus(event.getId(), RegistrationStatus.CONFIRMED)).isEqualTo(1);
        assertThat(users.findByEmailIgnoreCase("STUDENT@test.ro")).isPresent();
    }

    @Test
    void emailIsUniqueIgnoringCase() {
        assertThatThrownBy(() -> users.saveAndFlush(new User("ORG@Test.ro", "hash", "Dup", GlobalRole.STUDENT)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("ux_users_email_lower");
    }

    @Test
    void organizationNameIsUniqueIgnoringCase() {
        assertThatThrownBy(() -> organizations.saveAndFlush(new Organization("TEST ORG", null, null)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("ux_organizations_name_lower");
    }

    @Test
    void membershipIsUniquePerUserAndOrganization() {
        memberships.saveAndFlush(new OrganizationMembership(student, organization, OrganizationRole.MEMBER));

        assertThatThrownBy(() -> memberships.saveAndFlush(
                new OrganizationMembership(student, organization, OrganizationRole.ORGANIZER)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("uq_memberships_user_organization");
    }

    @Test
    void capacityMustBePositive() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        assertThatThrownBy(() -> events.saveAndFlush(newEvent(0, start, start.plus(1, ChronoUnit.HOURS))))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("ck_events_capacity_positive");
    }

    @Test
    void endMustBeAfterStart() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        assertThatThrownBy(() -> events.saveAndFlush(newEvent(10, start, start)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("ck_events_end_after_start");
    }

    @Test
    void registrationDeadlineMustNotBeAfterStart() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Event event = newEvent(10, start, start.plus(1, ChronoUnit.HOURS));
        event.setRegistrationDeadline(start.plus(1, ChronoUnit.MINUTES));

        assertThatThrownBy(() -> events.saveAndFlush(event))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("ck_events_deadline_before_start");
    }

    @Test
    void atMostOneConfirmedRegistrationPerUserAndEvent() {
        Event event = savedEvent();
        registrations.saveAndFlush(new Registration(event, student));

        assertThatThrownBy(() -> registrations.saveAndFlush(new Registration(event, student)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("ux_registrations_confirmed_once");
    }

    @Test
    void userCanRegisterAgainAfterCancelling() {
        Event event = savedEvent();
        Registration first = new Registration(event, student);
        first.setStatus(RegistrationStatus.CANCELLED);
        registrations.saveAndFlush(first);

        registrations.saveAndFlush(new Registration(event, student));

        assertThat(registrations.countByEventIdAndStatus(event.getId(), RegistrationStatus.CONFIRMED)).isEqualTo(1);
        assertThat(registrations.countByEventIdAndStatus(event.getId(), RegistrationStatus.CANCELLED)).isEqualTo(1);
    }

    @Test
    void onlyConfirmedRegistrationsCanBeCheckedIn() {
        Event event = savedEvent();
        Registration registration = new Registration(event, student);
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCheckedInAt(Instant.now());

        assertThatThrownBy(() -> registrations.saveAndFlush(registration))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("ck_registrations_checkin_confirmed");
    }

    @Test
    void eventWithRegistrationsCannotBeDeleted() {
        Event event = savedEvent();
        registrations.saveAndFlush(new Registration(event, student));
        entityManager.clear();

        assertThatThrownBy(() -> {
            events.deleteById(event.getId());
            events.flush();
        })
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining("fk_registrations_event");
    }

    @Test
    void deletingEventWithoutRegistrationsCascadesToTasks() {
        Event event = savedEvent();
        EventTask task = tasks.saveAndFlush(new EventTask(event, "Book room"));
        entityManager.clear();

        events.deleteById(event.getId());
        events.flush();
        entityManager.clear();

        assertThat(tasks.findById(task.getId())).isEmpty();
    }

    @Test
    void deletingMembershipUnassignsTasks() {
        Event event = savedEvent();
        OrganizationMembership membership =
                memberships.saveAndFlush(new OrganizationMembership(organizer, organization, OrganizationRole.ORGANIZER));
        EventTask task = new EventTask(event, "Print posters");
        task.setAssignee(membership);
        tasks.saveAndFlush(task);
        entityManager.clear();

        memberships.deleteById(membership.getId());
        memberships.flush();
        entityManager.clear();

        assertThat(tasks.findById(task.getId())).get()
                .extracting(EventTask::getAssignee).isNull();
    }
}
