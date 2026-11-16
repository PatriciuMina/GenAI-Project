package ro.ubb.events.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
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

/** Permission matrix checks for the write endpoints in W1, W2 and W5. */
class PermissionMatrixIntegrationTest extends IntegrationTestBase {

    private static final int DENIED = 403;
    private static final int UNAUTHENTICATED = 401;
    private static final int CREATED = 201;
    private static final int OK = 200;
    private static final int NO_CONTENT = 204;

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;

    private Organization organization;
    private User president;
    private User organizer;
    private User member;
    private User outsider;
    private User admin;
    private OrganizationMembership targetMembership;
    private Event draftEvent;
    private Event ongoingEvent;
    private Registration registration;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Permission matrix " + UUID.randomUUID(), null, null));
        president = memberWithRole(OrganizationRole.PRESIDENT);
        organizer = memberWithRole(OrganizationRole.ORGANIZER);
        member = memberWithRole(OrganizationRole.MEMBER);
        targetMembership = membershipRepository.save(new OrganizationMembership(
                createUser(uniqueEmail("target"), GlobalRole.STUDENT), organization, OrganizationRole.MEMBER));
        outsider = createUser(uniqueEmail("outsider"), GlobalRole.STUDENT);
        admin = createUser(uniqueEmail("admin"), GlobalRole.ADMIN);

        Instant startsAt = Instant.now().plus(Duration.ofDays(10));
        draftEvent = saveEvent(EventStatus.DRAFT, startsAt);
        ongoingEvent = saveEvent(EventStatus.ONGOING, startsAt);
        User participant = createUser(uniqueEmail("participant"), GlobalRole.STUDENT);
        registration = registrationRepository.save(new Registration(ongoingEvent, participant));
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registrationRepository.save(registration);
    }

    @ParameterizedTest(name = "add member: {0} -> {1}")
    @MethodSource("presidentOrAdminCreateCases")
    void addMemberPermissionMatrix(Actor actor, int expected) throws Exception {
        String newMemberEmail = uniqueEmail("new-member");
        createUser(newMemberEmail, GlobalRole.STUDENT);
        perform(post("/api/v1/organizations/{orgId}/members", organization.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", newMemberEmail, "role", "MEMBER"))), actor)
                .andExpect(status().is(expected));
    }

    @ParameterizedTest(name = "remove member: {0} -> {1}")
    @MethodSource("presidentOrAdminDeleteCases")
    void removeMemberPermissionMatrix(Actor actor, int expected) throws Exception {
        perform(delete("/api/v1/organizations/{orgId}/members/{membershipId}",
                organization.getId(), targetMembership.getId()), actor).andExpect(status().is(expected));
    }

    @ParameterizedTest(name = "create organization: {0} -> {1}")
    @MethodSource("adminOnlyCases")
    void createOrganizationPermissionMatrix(Actor actor, int expected) throws Exception {
        String presidentEmail = uniqueEmail("future-president");
        createUser(presidentEmail, GlobalRole.STUDENT);
        perform(post("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("name", "New organization " + UUID.randomUUID(),
                        "presidentEmail", presidentEmail))), actor).andExpect(status().is(expected));
    }

    @ParameterizedTest(name = "create event: {0} -> {1}")
    @MethodSource("organizerCreateCases")
    void createEventPermissionMatrix(Actor actor, int expected) throws Exception {
        Instant start = Instant.now().plus(Duration.ofDays(20));
        perform(post("/api/v1/organizations/{orgId}/events", organization.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("title", "Matrix event", "location", "FSEGA",
                        "category", "WORKSHOP", "startAt", start.toString(),
                        "endAt", start.plus(Duration.ofHours(2)).toString(), "capacity", 20))), actor)
                .andExpect(status().is(expected));
    }

    @ParameterizedTest(name = "change event status: {0} -> {1}")
    @MethodSource("organizerOkCases")
    void changeStatusPermissionMatrix(Actor actor, int expected) throws Exception {
        perform(post("/api/v1/events/{id}/status", draftEvent.getId())
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("status", "PUBLISHED"))), actor)
                .andExpect(status().is(expected));
    }

    @ParameterizedTest(name = "create task: {0} -> {1}")
    @MethodSource("organizerCreateCases")
    void createTaskPermissionMatrix(Actor actor, int expected) throws Exception {
        perform(post("/api/v1/events/{eventId}/tasks", draftEvent.getId())
                .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("title", "Prepare room"))), actor)
                .andExpect(status().is(expected));
    }

    @ParameterizedTest(name = "check in: {0} -> {1}")
    @MethodSource("organizerOkCases")
    void checkInPermissionMatrix(Actor actor, int expected) throws Exception {
        perform(post("/api/v1/events/{eventId}/registrations/{registrationId}/check-in",
                ongoingEvent.getId(), registration.getId()), actor).andExpect(status().is(expected));
    }

    @ParameterizedTest(name = "post announcement: {0} -> {1}")
    @MethodSource("organizerCreateCases")
    void postAnnouncementPermissionMatrix(Actor actor, int expected) throws Exception {
        perform(post("/api/v1/events/{eventId}/announcements", ongoingEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("title", "Room update", "body", "Use the main entrance."))), actor)
                .andExpect(status().is(expected));
    }

    private org.springframework.test.web.servlet.ResultActions perform(
            MockHttpServletRequestBuilder request, Actor actor) throws Exception {
        if (actor != Actor.NO_TOKEN) {
            User user = switch (actor) {
                case OUTSIDER -> outsider;
                case MEMBER -> member;
                case ORGANIZER -> organizer;
                case PRESIDENT -> president;
                case ADMIN -> admin;
                case NO_TOKEN -> throw new IllegalStateException("No token actor has no user");
            };
            request.header(HttpHeaders.AUTHORIZATION, bearer(loginAndGetToken(user.getEmail())));
        }
        return mockMvc.perform(request);
    }

    private User memberWithRole(OrganizationRole role) {
        User user = createUser(uniqueEmail(role.name().toLowerCase()), GlobalRole.STUDENT);
        membershipRepository.save(new OrganizationMembership(user, organization, role));
        return user;
    }

    private Event saveEvent(EventStatus status, Instant startsAt) {
        Event event = new Event(organization, organizer, "Event " + UUID.randomUUID(), "FSEGA",
                EventCategory.WORKSHOP, startsAt, startsAt.plus(Duration.ofHours(2)), 30);
        event.setStatus(status);
        return eventRepository.save(event);
    }

    static Stream<Arguments> presidentOrAdminCreateCases() {
        return Stream.of(Arguments.of(Actor.NO_TOKEN, UNAUTHENTICATED), Arguments.of(Actor.OUTSIDER, DENIED),
                Arguments.of(Actor.MEMBER, DENIED), Arguments.of(Actor.ORGANIZER, DENIED),
                Arguments.of(Actor.PRESIDENT, CREATED), Arguments.of(Actor.ADMIN, CREATED));
    }

    static Stream<Arguments> presidentOrAdminDeleteCases() {
        return Stream.of(Arguments.of(Actor.NO_TOKEN, UNAUTHENTICATED), Arguments.of(Actor.OUTSIDER, DENIED),
                Arguments.of(Actor.MEMBER, DENIED), Arguments.of(Actor.ORGANIZER, DENIED),
                Arguments.of(Actor.PRESIDENT, NO_CONTENT), Arguments.of(Actor.ADMIN, NO_CONTENT));
    }

    static Stream<Arguments> adminOnlyCases() {
        return Stream.of(Arguments.of(Actor.NO_TOKEN, UNAUTHENTICATED), Arguments.of(Actor.OUTSIDER, DENIED),
                Arguments.of(Actor.MEMBER, DENIED), Arguments.of(Actor.ORGANIZER, DENIED),
                Arguments.of(Actor.PRESIDENT, DENIED), Arguments.of(Actor.ADMIN, CREATED));
    }

    static Stream<Arguments> organizerOkCases() {
        return Stream.of(Arguments.of(Actor.NO_TOKEN, UNAUTHENTICATED), Arguments.of(Actor.OUTSIDER, DENIED),
                Arguments.of(Actor.MEMBER, DENIED), Arguments.of(Actor.ORGANIZER, OK),
                Arguments.of(Actor.PRESIDENT, OK), Arguments.of(Actor.ADMIN, DENIED));
    }

    static Stream<Arguments> organizerCreateCases() {
        return Stream.of(Arguments.of(Actor.NO_TOKEN, UNAUTHENTICATED), Arguments.of(Actor.OUTSIDER, DENIED),
                Arguments.of(Actor.MEMBER, DENIED), Arguments.of(Actor.ORGANIZER, CREATED),
                Arguments.of(Actor.PRESIDENT, CREATED), Arguments.of(Actor.ADMIN, DENIED));
    }

    enum Actor { NO_TOKEN, OUTSIDER, MEMBER, ORGANIZER, PRESIDENT, ADMIN }
}
