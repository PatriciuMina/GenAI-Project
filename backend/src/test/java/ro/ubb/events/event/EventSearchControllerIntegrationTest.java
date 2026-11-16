package ro.ubb.events.event;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.repository.EventRepository;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.support.IntegrationTestBase;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

class EventSearchControllerIntegrationTest extends IntegrationTestBase {

    @Autowired private EventRepository eventRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Organization organization;
    private long springId;
    private long unrelatedId;

    @BeforeEach
    void setUp() {
        User creator = createUser(uniqueEmail("search-creator"), GlobalRole.STUDENT);
        organization = organizationRepository.save(new Organization("Search Org " + UUID.randomUUID(), null, null));
        Instant base = Instant.now().plusSeconds(3600);
        Event spring = saveEvent(creator, "Intro to Spring Boot Workshop", EventCategory.WORKSHOP,
                base, base.plusSeconds(3600), EventStatus.PUBLISHED);
        springId = spring.getId();
        Event other = saveEvent(creator, "Career Networking", EventCategory.CAREER,
                base.plusSeconds(7200), base.plusSeconds(10800), EventStatus.REGISTRATION_CLOSED);
        unrelatedId = other.getId();
        saveEvent(creator, "Ongoing cleanup", EventCategory.VOLUNTEERING,
                base.minusSeconds(7200), base.plusSeconds(7200), EventStatus.ONGOING);
        saveEvent(creator, "Hidden draft", EventCategory.WORKSHOP,
                base, base.plusSeconds(3600), EventStatus.DRAFT);
        saveEvent(creator, "Completed", EventCategory.WORKSHOP,
                base, base.plusSeconds(3600), EventStatus.COMPLETED);
        saveEvent(creator, "Expired", EventCategory.WORKSHOP,
                base.minusSeconds(7200), Instant.now().minusSeconds(60), EventStatus.PUBLISHED);
    }

    @Test
    void defaultSearchIncludesOnlyVisibleUnendedEventsAndCountsRegistrations() throws Exception {
        User attendee = createUser(uniqueEmail("search-attendee"), GlobalRole.STUDENT);
        jdbcTemplate.update("INSERT INTO registrations (event_id, user_id, status, registered_at) VALUES (?, ?, 'CONFIRMED', now())",
                springId, attendee.getId());

        mockMvc.perform(get("/api/v1/events").param("organizationId", organization.getId().toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenForNewStudent())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].title").value("Ongoing cleanup"))
                .andExpect(jsonPath("$.content[1].title").value("Intro to Spring Boot Workshop"))
                .andExpect(jsonPath("$.content[1].confirmedCount").value(1))
                .andExpect(jsonPath("$.content[1].freePlaces").value(29))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void filtersCanBeCombinedAndPagingIsCapped() throws Exception {
        Instant from = Instant.now().plusSeconds(3000);
        Instant to = Instant.now().plusSeconds(5000);
        mockMvc.perform(get("/api/v1/events")
                        .param("q", "sPrInG")
                        .param("category", "WORKSHOP")
                        .param("organizationId", organization.getId().toString())
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("page", "0").param("size", "100")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenForNewStudent())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(springId))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/v1/events").param("organizationId", organization.getId().toString())
                        .param("category", "CAREER")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenForNewStudent())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(unrelatedId));
    }

    @Test
    void invalidFiltersAndPagingReturnInvalidParameter() throws Exception {
        String auth = bearer(tokenForNewStudent());
        mockMvc.perform(get("/api/v1/events").param("category", "NOT_A_CATEGORY")
                        .header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        mockMvc.perform(get("/api/v1/events").param("from", "not-a-date")
                        .header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        mockMvc.perform(get("/api/v1/events").param("from", "2026-12-31T00:00:00Z")
                        .param("to", "2026-12-01T00:00:00Z").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        mockMvc.perform(get("/api/v1/events").param("page", "-1").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    private Event saveEvent(User creator, String title, EventCategory category, Instant start, Instant end,
                            EventStatus status) {
        Event event = new Event(organization, creator, title, "FMI, room 2/I", category, start, end, 30);
        event.setStatus(status);
        return eventRepository.save(event);
    }
}
