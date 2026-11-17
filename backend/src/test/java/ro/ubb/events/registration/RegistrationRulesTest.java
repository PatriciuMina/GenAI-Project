package ro.ubb.events.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

/**
 * All W4 registration rules in one place (T-36), one rule per test, through the real API and database:
 * registration window, duplicates, cancel / register again, and when cancelling is not allowed.
 */
@DisplayName("W4 registration rules")
class RegistrationRulesTest extends IntegrationTestBase {

    private static final String REGISTER_URL = "/api/v1/events/{eventId}/registrations";
    private static final String CANCEL_URL = "/api/v1/events/{eventId}/registrations/me";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;

    // --- registration window ---

    @Test
    @DisplayName("A PUBLISHED event before its deadline accepts the registration (201, CONFIRMED)")
    void openEventAcceptsRegistration() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofDays(10), Duration.ofDays(9));

        register(event, tokenForNewStudent())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("A DRAFT event is hidden from students: registering returns 404, not REGISTRATION_CLOSED")
    void draftEventIsHiddenAs404() throws Exception {
        Event event = event(EventStatus.DRAFT, Duration.ofDays(10), null);

        register(event, tokenForNewStudent()).andExpect(status().isNotFound());
        assertThat(registrationRepository.findParticipants(event.getId())).isEmpty();
    }

    @ParameterizedTest(name = "{0} event returns 409 REGISTRATION_CLOSED")
    @EnumSource(value = EventStatus.class, names = {"REGISTRATION_CLOSED", "ONGOING", "COMPLETED", "CANCELLED"})
    @DisplayName("An event that is not PUBLISHED does not accept registrations")
    void eventNotPublishedReturnsRegistrationClosed(EventStatus status) throws Exception {
        Event event = event(status, Duration.ofDays(10), null);

        expectConflict(register(event, tokenForNewStudent()), "REGISTRATION_CLOSED");
        assertThat(registrationRepository.findParticipants(event.getId())).isEmpty();
    }

    @Test
    @DisplayName("A PUBLISHED event whose registration deadline has passed returns 409 REGISTRATION_CLOSED")
    void passedDeadlineReturnsRegistrationClosed() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofDays(10), Duration.ofHours(-1));

        expectConflict(register(event, tokenForNewStudent()), "REGISTRATION_CLOSED");
    }

    @Test
    @DisplayName("Without a deadline, a PUBLISHED event that already started returns 409 REGISTRATION_CLOSED")
    void noDeadlineAndStartPassedReturnsRegistrationClosed() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofHours(-1), null);

        expectConflict(register(event, tokenForNewStudent()), "REGISTRATION_CLOSED");
    }

    // --- duplicates ---

    @Test
    @DisplayName("Registering twice for the same event returns 409 ALREADY_REGISTERED and keeps one row")
    void secondRegistrationReturnsAlreadyRegistered() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofDays(10), null);
        String token = tokenForNewStudent();
        register(event, token).andExpect(status().isCreated());

        expectConflict(register(event, token), "ALREADY_REGISTERED");
        assertThat(registrationRepository.findParticipants(event.getId())).hasSize(1);
    }

    // --- cancel and register again ---

    @Test
    @DisplayName("Cancel then register again creates a new CONFIRMED row; the old row stays CANCELLED")
    void cancelThenRegisterAgainKeepsBothRows() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofDays(10), null);
        User student = student();
        String token = loginAndGetToken(student.getEmail());
        register(event, token).andExpect(status().isCreated());
        Long firstId = confirmedRowOf(event, student).getId();

        cancel(event, token).andExpect(status().isNoContent());
        register(event, token).andExpect(status().isCreated());

        List<Registration> rows = registrationRepository.findParticipants(event.getId());
        assertThat(rows).hasSize(2);
        Registration old = rows.stream().filter(r -> r.getId().equals(firstId)).findFirst().orElseThrow();
        Registration current = rows.stream().filter(r -> !r.getId().equals(firstId)).findFirst().orElseThrow();
        assertThat(old.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
        assertThat(current.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
        assertThat(current.getUser().getId()).isEqualTo(student.getId());
    }

    // --- when cancelling is not allowed ---

    @Test
    @DisplayName("Cancelling after the event started (still PUBLISHED) returns 409 CANCEL_NOT_ALLOWED")
    void cancelAfterStartReturnsCancelNotAllowed() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofHours(-1), null);
        User student = student();
        registrationRepository.save(new Registration(event, student));

        expectConflict(cancel(event, loginAndGetToken(student.getEmail())), "CANCEL_NOT_ALLOWED");
        assertThat(confirmedRowOf(event, student).getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Cancelling without a registration returns 404 NOT_REGISTERED")
    void cancelWhenNotRegisteredReturnsNotRegistered() throws Exception {
        Event event = event(EventStatus.PUBLISHED, Duration.ofDays(10), null);

        cancel(event, tokenForNewStudent())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_REGISTERED"));
    }

    // --- helpers ---

    private ResultActions register(Event event, String token) throws Exception {
        return mockMvc.perform(post(REGISTER_URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    private ResultActions cancel(Event event, String token) throws Exception {
        return mockMvc.perform(delete(CANCEL_URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token)));
    }

    private static void expectConflict(ResultActions result, String code) throws Exception {
        result.andExpect(status().isConflict()).andExpect(jsonPath("$.code").value(code));
    }

    private Registration confirmedRowOf(Event event, User user) {
        return registrationRepository
                .findByEventIdAndUserIdAndStatus(event.getId(), user.getId(), RegistrationStatus.CONFIRMED)
                .orElseThrow();
    }

    private User student() {
        return createUser(uniqueEmail("student"), GlobalRole.STUDENT);
    }

    /**
     * An event of a fresh organization with capacity 10 that starts {@code startFromNow} from now (may be
     * negative) and has a registration deadline {@code deadlineFromNow} from now, or none if null.
     */
    private Event event(EventStatus status, Duration startFromNow, Duration deadlineFromNow) {
        Organization organization = organizationRepository.save(
                new Organization("Org " + UUID.randomUUID(), null, null));
        Instant start = Instant.now().plus(startFromNow);
        Event event = new Event(organization, student(), "Event " + UUID.randomUUID(), "FSEGA",
                EventCategory.WORKSHOP, start, start.plus(Duration.ofHours(3)), 10);
        event.setStatus(status);
        if (deadlineFromNow != null) {
            event.setRegistrationDeadline(Instant.now().plus(deadlineFromNow));
        }
        return eventRepository.save(event);
    }
}
