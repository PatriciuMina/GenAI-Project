package ro.ubb.events.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

class RegistrationCancelControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/events/{eventId}/registrations/me";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;

    @Test
    void withoutTokenReturns401() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofDays(10));

        mockMvc.perform(delete(URL, event.getId())).andExpect(status().isUnauthorized());
    }

    @Test
    void studentCancelsFreesThePlaceAndCanRegisterAgain() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofDays(10));
        User student = student();
        String token = loginAndGetToken(student.getEmail());
        register(event, token).andExpect(status().isCreated());
        expectPlaces(event, token, 1, 9);

        cancel(event, token).andExpect(status().isNoContent());

        expectPlaces(event, token, 0, 10);
        assertThat(registrationRepository.findParticipants(event.getId()))
                .extracting(Registration::getStatus)
                .containsExactly(RegistrationStatus.CANCELLED);

        register(event, token).andExpect(status().isCreated());
        expectPlaces(event, token, 1, 9);
        assertThat(registrationRepository.findParticipants(event.getId()))
                .extracting(Registration::getStatus)
                .containsExactlyInAnyOrder(RegistrationStatus.CANCELLED, RegistrationStatus.CONFIRMED);
    }

    @Test
    void cancellingTwiceReturns404NotRegistered() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofDays(10));
        String token = loginAndGetToken(student().getEmail());
        register(event, token).andExpect(status().isCreated());
        cancel(event, token).andExpect(status().isNoContent());

        cancel(event, token)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_REGISTERED"));
    }

    @Test
    void studentWhoNeverRegisteredGets404NotRegistered() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofDays(10));

        cancel(event, loginAndGetToken(student().getEmail()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_REGISTERED"));
    }

    @Test
    void onlyTheCallersOwnRegistrationIsCancelled() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofDays(10));
        User registered = student();
        confirmedRegistration(event, registered);

        cancel(event, loginAndGetToken(student().getEmail()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_REGISTERED"));

        assertThat(registrationRepository.findByEventIdAndUserIdAndStatus(
                event.getId(), registered.getId(), RegistrationStatus.CONFIRMED)).isPresent();
    }

    @Test
    void cancellingIsAllowedWhileRegistrationIsClosedButTheEventHasNotStarted() throws Exception {
        Event event = event(EventStatus.REGISTRATION_CLOSED, Duration.ofDays(10));
        User student = student();
        confirmedRegistration(event, student);

        cancel(event, loginAndGetToken(student.getEmail())).andExpect(status().isNoContent());
    }

    @Test
    void ongoingEventReturns409CancelNotAllowed() throws Exception {
        Event event = event(EventStatus.ONGOING, Duration.ofHours(-1));
        User student = student();
        confirmedRegistration(event, student);

        cancel(event, loginAndGetToken(student.getEmail()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CANCEL_NOT_ALLOWED"));

        assertThat(registrationRepository.findByEventIdAndUserIdAndStatus(
                event.getId(), student.getId(), RegistrationStatus.CONFIRMED)).isPresent();
    }

    @Test
    void checkedInRegistrationReturns409CancelNotAllowed() throws Exception {
        Event event = event(EventStatus.REGISTRATION_CLOSED, Duration.ofDays(10));
        User student = student();
        Registration registration = new Registration(event, student);
        registration.checkIn(Instant.now());
        registrationRepository.save(registration);

        cancel(event, loginAndGetToken(student.getEmail()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CANCEL_NOT_ALLOWED"));
    }

    private ResultActions cancel(Event event, String token) throws Exception {
        return mockMvc.perform(delete(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    private ResultActions register(Event event, String token) throws Exception {
        return mockMvc.perform(post("/api/v1/events/{eventId}/registrations", event.getId())
                .header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    private void expectPlaces(Event event, String token, int confirmed, int free) throws Exception {
        mockMvc.perform(get("/api/v1/events/{id}", event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedCount").value(confirmed))
                .andExpect(jsonPath("$.freePlaces").value(free));
    }

    private void confirmedRegistration(Event event, User user) {
        registrationRepository.save(new Registration(event, user));
    }

    private User student() {
        return createUser(uniqueEmail("student"), GlobalRole.STUDENT);
    }

    /** An event of a fresh organization with capacity 10, starting {@code startFromNow} from now (may be negative). */
    private Event event(EventStatus status, Duration startFromNow) {
        Organization organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        Instant start = Instant.now().plus(startFromNow);
        Event event = new Event(organization, student(), "Event " + UUID.randomUUID(), "FSEGA",
                EventCategory.WORKSHOP, start, start.plus(Duration.ofHours(3)), 10);
        event.setStatus(status);
        return eventRepository.save(event);
    }
}
