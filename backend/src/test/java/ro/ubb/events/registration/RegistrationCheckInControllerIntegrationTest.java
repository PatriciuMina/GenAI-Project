package ro.ubb.events.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.repository.EventRepository;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.repository.RegistrationRepository;
import ro.ubb.events.support.IntegrationTestBase;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/** POST /api/v1/events/{eventId}/registrations/{registrationId}/check-in end to end. */
class RegistrationCheckInControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/events/{eventId}/registrations/{registrationId}/check-in";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;

    private Organization organization;
    private User organizer;
    private Event event;
    private Registration registration;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        organizer = memberWithRole(organization, OrganizationRole.ORGANIZER);
        Instant start = Instant.now().plus(Duration.ofDays(1));
        event = new Event(organization, organizer, "Reading Marathon", "Hall", EventCategory.CULTURAL,
                start, start.plus(Duration.ofHours(8)), 30);
        event.setStatus(EventStatus.ONGOING);
        event = eventRepository.save(event);
        registration = register(event, RegistrationStatus.CONFIRMED);
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mockMvc.perform(post(URL, event.getId(), registration.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void organizerChecksInThenSecondAttemptConflicts() throws Exception {
        String token = bearer(loginAndGetToken(organizer.getEmail()));

        mockMvc.perform(post(URL, event.getId(), registration.getId()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationId").value(registration.getId()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.checkedInAt").isNotEmpty());
        assertThat(registrationRepository.findById(registration.getId()).orElseThrow().getCheckedInAt())
                .isNotNull();

        mockMvc.perform(post(URL, event.getId(), registration.getId()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_CHECKED_IN"));
    }

    @Test
    void presidentMayCheckInToo() throws Exception {
        User president = memberWithRole(organization, OrganizationRole.PRESIDENT);

        mockMvc.perform(post(URL, event.getId(), registration.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(president.getEmail()))))
                .andExpect(status().isOk());
    }

    @Test
    void publishedEventReturns409CheckInNotAllowed() throws Exception {
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);

        mockMvc.perform(post(URL, event.getId(), registration.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(organizer.getEmail()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CHECK_IN_NOT_ALLOWED"));
    }

    @Test
    void cancelledRegistrationReturns409CheckInNotAllowed() throws Exception {
        Registration cancelled = register(event, RegistrationStatus.CANCELLED);

        mockMvc.perform(post(URL, event.getId(), cancelled.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(organizer.getEmail()))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CHECK_IN_NOT_ALLOWED"));
    }

    @Test
    void organizerOfAnotherOrganizationReturns403() throws Exception {
        Organization other = organizationRepository.save(new Organization("Other " + UUID.randomUUID(), null, null));
        User foreign = memberWithRole(other, OrganizationRole.ORGANIZER);

        mockMvc.perform(post(URL, event.getId(), registration.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(foreign.getEmail()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void plainMemberReturns403() throws Exception {
        User member = memberWithRole(organization, OrganizationRole.MEMBER);

        mockMvc.perform(post(URL, event.getId(), registration.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(member.getEmail()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownRegistrationReturns404() throws Exception {
        mockMvc.perform(post(URL, event.getId(), 999_999_999L)
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(organizer.getEmail()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrationOfAnotherEventReturns404() throws Exception {
        Instant start = Instant.now().plus(Duration.ofDays(2));
        Event otherEvent = new Event(organization, organizer, "Other", "Hall", EventCategory.CULTURAL,
                start, start.plus(Duration.ofHours(2)), 30);
        otherEvent.setStatus(EventStatus.ONGOING);
        otherEvent = eventRepository.save(otherEvent);
        Registration foreign = register(otherEvent, RegistrationStatus.CONFIRMED);

        mockMvc.perform(post(URL, event.getId(), foreign.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(organizer.getEmail()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownEventReturns404() throws Exception {
        mockMvc.perform(post(URL, 999_999_999L, registration.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(organizer.getEmail()))))
                .andExpect(status().isNotFound());
    }

    private Registration register(Event forEvent, RegistrationStatus status) {
        User user = userRepository.save(new User(uniqueEmail("participant"),
                passwordEncoder.encode(PASSWORD), "Maria Dobre", GlobalRole.STUDENT));
        Registration r = new Registration(forEvent, user);
        r.setStatus(status);
        return registrationRepository.save(r);
    }

    private User memberWithRole(Organization org, OrganizationRole role) {
        User user = createUser(uniqueEmail(role.name().toLowerCase()), GlobalRole.STUDENT);
        membershipRepository.save(new OrganizationMembership(user, org, role));
        return user;
    }
}
