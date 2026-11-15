package ro.ubb.events.registration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
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

/** GET /api/v1/users/me/registrations end to end: only my rows, newest first, CONFIRMED and CANCELLED. */
class MyRegistrationsControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/users/me/registrations";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;

    private Organization organization;
    private User student;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        student = createUser(uniqueEmail("student"), GlobalRole.STUDENT);
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void returnsMyRegistrationsNewestFirstIncludingCancelledOnes() throws Exception {
        Event older = event("Older workshop", EventStatus.COMPLETED);
        Event newer = event("Newer workshop", EventStatus.PUBLISHED);
        Registration oldRegistration = register(student, older, RegistrationStatus.CANCELLED, Duration.ofDays(5));
        Registration newRegistration = register(student, newer, RegistrationStatus.CONFIRMED, Duration.ofDays(1));

        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(student.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(newRegistration.getId()))
                .andExpect(jsonPath("$[0].eventId").value(newer.getId()))
                .andExpect(jsonPath("$[0].eventTitle").value("Newer workshop"))
                .andExpect(jsonPath("$[0].eventStartAt").value(newer.getStartAt().toString()))
                .andExpect(jsonPath("$[0].eventLocation").value("FSEGA"))
                .andExpect(jsonPath("$[0].eventStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].registeredAt").isNotEmpty())
                .andExpect(jsonPath("$[0].checkedInAt").doesNotExist())
                .andExpect(jsonPath("$[1].id").value(oldRegistration.getId()))
                .andExpect(jsonPath("$[1].status").value("CANCELLED"))
                .andExpect(jsonPath("$[1].eventStatus").value("COMPLETED"));
    }

    @Test
    void doesNotIncludeOtherUsersRegistrations() throws Exception {
        User other = createUser(uniqueEmail("other"), GlobalRole.STUDENT);
        Event event = event("Shared workshop", EventStatus.PUBLISHED);
        register(student, event, RegistrationStatus.CONFIRMED, Duration.ofDays(1));
        register(other, event, RegistrationStatus.CONFIRMED, Duration.ofDays(2));

        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(other.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void userWithoutRegistrationsGetsAnEmptyList() throws Exception {
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, bearer(tokenForNewStudent())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private Event event(String title, EventStatus status) {
        Instant start = Instant.now().plus(Duration.ofDays(10)).truncatedTo(ChronoUnit.SECONDS);
        Event event = new Event(organization, student, title, "FSEGA", EventCategory.WORKSHOP,
                start, start.plus(Duration.ofHours(2)), 30);
        event.setStatus(status);
        return eventRepository.save(event);
    }

    private Registration register(User user, Event event, RegistrationStatus status, Duration registeredAgo) {
        Registration registration = new Registration(event, user);
        registration.setStatus(status);
        ReflectionTestUtils.setField(registration, "registeredAt", Instant.now().minus(registeredAgo));
        return registrationRepository.save(registration);
    }
}
