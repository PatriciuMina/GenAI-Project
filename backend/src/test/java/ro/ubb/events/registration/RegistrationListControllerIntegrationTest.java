package ro.ubb.events.registration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

/** GET /api/v1/events/{eventId}/registrations end to end: permissions, sorting and status filter. */
class RegistrationListControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/events/{eventId}/registrations";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;

    private Organization organization;
    private User president;
    private User organizer;
    private User member;
    private Event event;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        president = memberWithRole(organization, OrganizationRole.PRESIDENT);
        organizer = memberWithRole(organization, OrganizationRole.ORGANIZER);
        member = memberWithRole(organization, OrganizationRole.MEMBER);
        Instant start = Instant.now().plus(Duration.ofDays(10));
        event = eventRepository.save(new Event(organization, organizer, "Career Fair", "FSEGA",
                EventCategory.WORKSHOP, start, start.plus(Duration.ofHours(2)), 30));
    }

    @Test
    void withoutTokenReturns401() throws Exception {
        mockMvc.perform(get(URL, event.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void organizerSeesParticipantsSortedByFullName() throws Exception {
        User zoe = register("Zoe Zamfir", RegistrationStatus.CONFIRMED);
        User andrei = register("Andrei Toma", RegistrationStatus.CONFIRMED);
        User maria = register("Maria Dobre", RegistrationStatus.CONFIRMED);

        mockMvc.perform(get(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].fullName").value("Andrei Toma"))
                .andExpect(jsonPath("$[0].userId").value(andrei.getId()))
                .andExpect(jsonPath("$[0].email").value(andrei.getEmail()))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].registrationId").isNumber())
                .andExpect(jsonPath("$[0].registeredAt").isNotEmpty())
                .andExpect(jsonPath("$[0].checkedInAt").doesNotExist())
                .andExpect(jsonPath("$[1].fullName").value(maria.getFullName()))
                .andExpect(jsonPath("$[2].fullName").value(zoe.getFullName()));
    }

    @Test
    void presidentMaySeeParticipantsToo() throws Exception {
        register("Andrei Toma", RegistrationStatus.CONFIRMED);

        mockMvc.perform(get(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token(president))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void statusFilterKeepsOnlyMatchingRegistrations() throws Exception {
        register("Andrei Toma", RegistrationStatus.CONFIRMED);
        register("Maria Dobre", RegistrationStatus.CANCELLED);

        mockMvc.perform(get(URL, event.getId()).param("status", "CONFIRMED")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Andrei Toma"));
        mockMvc.perform(get(URL, event.getId()).param("status", "CANCELLED")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Maria Dobre"));
    }

    @Test
    void plainMemberReturns403() throws Exception {
        mockMvc.perform(get(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token(member))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void organizerOfAnotherOrganizationReturns403() throws Exception {
        Organization other = organizationRepository.save(new Organization("Other " + UUID.randomUUID(), null, null));
        User foreignOrganizer = memberWithRole(other, OrganizationRole.ORGANIZER);

        mockMvc.perform(get(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token(foreignOrganizer))))
                .andExpect(status().isForbidden());
    }

    @Test
    void unknownEventReturns404() throws Exception {
        mockMvc.perform(get(URL, 999_999_999L).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void invalidStatusReturns400() throws Exception {
        mockMvc.perform(get(URL, event.getId()).param("status", "WAITING")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isBadRequest());
    }

    private User register(String fullName, RegistrationStatus status) {
        User user = userRepository.save(new User(uniqueEmail("participant"),
                passwordEncoder.encode(PASSWORD), fullName, GlobalRole.STUDENT));
        Registration registration = new Registration(event, user);
        registration.setStatus(status);
        registrationRepository.save(registration);
        return user;
    }

    private User memberWithRole(Organization org, OrganizationRole role) {
        User user = createUser(uniqueEmail(role.name().toLowerCase()), GlobalRole.STUDENT);
        membershipRepository.save(new OrganizationMembership(user, org, role));
        return user;
    }

    private String token(User user) throws Exception {
        return loginAndGetToken(user.getEmail());
    }
}
