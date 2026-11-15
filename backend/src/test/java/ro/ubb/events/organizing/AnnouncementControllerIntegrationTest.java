package ro.ubb.events.organizing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.repository.EventRepository;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.entity.OrganizationMembership;
import ro.ubb.events.organization.entity.OrganizationRole;
import ro.ubb.events.organization.repository.OrganizationMembershipRepository;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.organizing.entity.Announcement;
import ro.ubb.events.organizing.repository.AnnouncementRepository;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.repository.RegistrationRepository;
import ro.ubb.events.support.IntegrationTestBase;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/** GET/POST /api/v1/events/{eventId}/announcements end to end. */
class AnnouncementControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL = "/api/v1/events/{eventId}/announcements";

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMembershipRepository membershipRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;
    @Autowired private AnnouncementRepository announcementRepository;

    private Organization organization;
    private User organizer;
    private User member;
    private Event event;

    @BeforeEach
    void setUp() {
        organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        organizer = memberWithRole(organization, OrganizationRole.ORGANIZER);
        member = memberWithRole(organization, OrganizationRole.MEMBER);
        event = eventWithStatus(EventStatus.PUBLISHED);
    }

    // ---- GET

    @Test
    void listWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get(URL, event.getId())).andExpect(status().isUnauthorized());
    }

    @Test
    void registeredUserReadsAnnouncementsNewestFirst() throws Exception {
        Announcement older = announcementRepository.saveAndFlush(
                new Announcement(event, organizer, "Bring your laptop", "Install JDK 21."));
        Thread.sleep(20);
        Announcement newer = announcementRepository.saveAndFlush(
                new Announcement(event, organizer, "Room changed", "We moved to room 5/I."));
        User student = registered(RegistrationStatus.CONFIRMED);

        mockMvc.perform(get(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token(student))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(newer.getId()))
                .andExpect(jsonPath("$[0].title").value("Room changed"))
                .andExpect(jsonPath("$[0].body").value("We moved to room 5/I."))
                .andExpect(jsonPath("$[0].authorName").value(organizer.getFullName()))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1].id").value(older.getId()));
    }

    @Test
    void userWithACancelledRegistrationMayStillRead() throws Exception {
        User student = registered(RegistrationStatus.CANCELLED);

        mockMvc.perform(get(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token(student))))
                .andExpect(status().isOk());
    }

    @Test
    void plainOrganizationMemberMayRead() throws Exception {
        mockMvc.perform(get(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(token(member))))
                .andExpect(status().isOk());
    }

    @Test
    void unrelatedUserGets403() throws Exception {
        String stranger = tokenForNewStudent();

        mockMvc.perform(get(URL, event.getId()).header(HttpHeaders.AUTHORIZATION, bearer(stranger)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void listOfUnknownEventReturns404() throws Exception {
        mockMvc.perform(get(URL, 999_999_999L).header(HttpHeaders.AUTHORIZATION, bearer(token(organizer))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // ---- POST

    @Test
    void organizerPostsAnAnnouncement() throws Exception {
        postAnnouncement(event.getId(), token(organizer), Map.of("title", "Room changed", "body", "We moved to room 5/I."))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Room changed"))
                .andExpect(jsonPath("$.body").value("We moved to room 5/I."))
                .andExpect(jsonPath("$.authorName").value(organizer.getFullName()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        assertThat(announcementRepository.findByEventIdOrderByCreatedAtDesc(event.getId())).hasSize(1);
    }

    @Test
    void plainMemberCannotPost() throws Exception {
        postAnnouncement(event.getId(), token(member), Map.of("title", "t", "body", "b"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void cannotPostOnADraftEvent() throws Exception {
        Event draft = eventWithStatus(EventStatus.DRAFT);

        postAnnouncement(draft.getId(), token(organizer), Map.of("title", "t", "body", "b"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ANNOUNCEMENT_NOT_ALLOWED"));
    }

    @Test
    void cannotPostOnACompletedEvent() throws Exception {
        Event completed = eventWithStatus(EventStatus.COMPLETED);

        postAnnouncement(completed.getId(), token(organizer), Map.of("title", "t", "body", "b"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ANNOUNCEMENT_NOT_ALLOWED"));
    }

    @Test
    void blankTitleOrBodyOrTooLongTitleReturns400() throws Exception {
        postAnnouncement(event.getId(), token(organizer), Map.of("title", " ", "body", "b"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
        postAnnouncement(event.getId(), token(organizer), Map.of("title", "t", "body", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("body"));
        postAnnouncement(event.getId(), token(organizer), Map.of("title", "x".repeat(201), "body", "b"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
    }

    @Test
    void postOnUnknownEventReturns404() throws Exception {
        postAnnouncement(999_999_999L, token(organizer), Map.of("title", "t", "body", "b"))
                .andExpect(status().isNotFound());
    }

    // ---- helpers

    private ResultActions postAnnouncement(Long eventId, String token, Object body) throws Exception {
        return mockMvc.perform(post(URL, eventId)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)));
    }

    private Event eventWithStatus(EventStatus status) {
        Instant start = Instant.now().plus(Duration.ofDays(10));
        Event e = new Event(organization, organizer, "Workshop " + UUID.randomUUID(), "FSEGA",
                EventCategory.WORKSHOP, start, start.plus(Duration.ofHours(2)), 30);
        e.setStatus(status);
        return eventRepository.save(e);
    }

    private User registered(RegistrationStatus status) {
        User user = createUser(uniqueEmail("student"), GlobalRole.STUDENT);
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
