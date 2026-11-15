package ro.ubb.events.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.repository.EventRepository;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.repository.RegistrationRepository;
import ro.ubb.events.support.IntegrationTestBase;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/** POST /api/v1/events/{eventId}/registrations end to end: the W4 rules with a real database. */
class RegistrationRegisterControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/events/{eventId}/registrations";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;

    @Test
    void withoutTokenReturns401() throws Exception {
        Event event = event(EventStatus.PUBLISHED, 10, Duration.ofDays(5));

        mockMvc.perform(post(URL, event.getId())).andExpect(status().isUnauthorized());
    }

    @Test
    void studentRegistersAndFreePlacesDropByOne() throws Exception {
        Event event = event(EventStatus.PUBLISHED, 10, Duration.ofDays(5));
        User student = student();
        String token = loginAndGetToken(student.getEmail());

        mockMvc.perform(post(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.eventId").value(event.getId()))
                .andExpect(jsonPath("$.eventTitle").value(event.getTitle()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.registeredAt").isNotEmpty())
                .andExpect(jsonPath("$.checkedInAt").doesNotExist());

        mockMvc.perform(get("/api/v1/events/{id}", event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedCount").value(1))
                .andExpect(jsonPath("$.freePlaces").value(9));
    }

    @Test
    void registeringTwiceReturns409AlreadyRegistered() throws Exception {
        Event event = event(EventStatus.PUBLISHED, 10, Duration.ofDays(5));
        String token = loginAndGetToken(student().getEmail());

        register(event, token).andExpect(status().isCreated());
        register(event, token)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_REGISTERED"));
    }

    @Test
    void fullEventReturns409EventFull() throws Exception {
        Event event = event(EventStatus.PUBLISHED, 1, Duration.ofDays(5));
        String first = loginAndGetToken(student().getEmail());
        String second = loginAndGetToken(student().getEmail());

        register(event, first).andExpect(status().isCreated());
        register(event, second)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EVENT_FULL"));
    }

    @Test
    void registrationClosedStatusReturns409RegistrationClosed() throws Exception {
        Event event = event(EventStatus.REGISTRATION_CLOSED, 10, Duration.ofDays(5));

        register(event, loginAndGetToken(student().getEmail()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REGISTRATION_CLOSED"));
    }

    @Test
    void passedDeadlineReturns409RegistrationClosed() throws Exception {
        Event event = event(EventStatus.PUBLISHED, 10, Duration.ofMinutes(-5));

        register(event, loginAndGetToken(student().getEmail()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REGISTRATION_CLOSED"));
    }

    @Test
    void cancelledStudentCanRegisterAgainAndKeepsTheHistoryRow() throws Exception {
        Event event = event(EventStatus.PUBLISHED, 10, Duration.ofDays(5));
        User student = student();
        Registration cancelled = new Registration(event, student);
        cancelled.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(cancelled);

        register(event, loginAndGetToken(student.getEmail())).andExpect(status().isCreated());

        assertThat(registrationRepository.findParticipants(event.getId()))
                .extracting(Registration::getStatus)
                .containsExactlyInAnyOrder(RegistrationStatus.CANCELLED, RegistrationStatus.CONFIRMED);
    }

    @Test
    void draftEventReturns404() throws Exception {
        Event event = event(EventStatus.DRAFT, 10, Duration.ofDays(5));

        register(event, loginAndGetToken(student().getEmail()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void unknownEventReturns404() throws Exception {
        mockMvc.perform(post(URL, 999_999_999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(student().getEmail()))))
                .andExpect(status().isNotFound());
    }

    private ResultActions register(Event event, String token) throws Exception {
        return mockMvc.perform(post(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    private User student() {
        return createUser(uniqueEmail("student"), GlobalRole.STUDENT);
    }

    /** Creates an event of a fresh organization; the deadline is {@code deadlineFromNow} (may be negative). */
    private Event event(EventStatus status, int capacity, Duration deadlineFromNow) {
        Organization organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        User creator = student();
        Instant start = Instant.now().plus(Duration.ofDays(10));
        Event event = new Event(organization, creator, "Event " + UUID.randomUUID(), "FSEGA",
                EventCategory.WORKSHOP, start, start.plus(Duration.ofHours(2)), capacity);
        event.setStatus(status);
        event.setRegistrationDeadline(Instant.now().plus(deadlineFromNow));
        return eventRepository.save(event);
    }
}
