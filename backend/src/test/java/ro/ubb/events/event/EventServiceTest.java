package ro.ubb.events.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.event.dto.EventRequest;
import ro.ubb.events.event.dto.EventResponse;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.mapper.EventMapper;
import ro.ubb.events.event.repository.EventRepository;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.service.OrganizationService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Unit test: repository and other features' services are mocked, the mapper is real. */
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    private static final Long ORG_ID = 1L;
    private static final Long CREATOR_ID = 4L;
    private static final Instant START = Instant.parse("2026-12-10T16:00:00Z");
    private static final Instant END = START.plus(Duration.ofHours(2));
    private static final Instant DEADLINE = Instant.parse("2026-12-09T22:00:00Z");

    @Mock private EventRepository eventRepository;
    @Mock private OrganizationService organizationService;
    @Mock private UserService userService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, organizationService, userService, new EventMapper());
    }

    @Test
    void createSavesADraftEventOfTheOrganizationWithTheCallerAsCreator() {
        Organization organization = new Organization("UBB Computer Science Society", null, null);
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        User creator = new User("elena@test.ro", "hash", "Elena Marin", GlobalRole.STUDENT);
        when(organizationService.getEntityById(ORG_ID)).thenReturn(organization);
        when(userService.getEntityById(CREATOR_ID)).thenReturn(creator);
        when(eventRepository.saveAndFlush(any(Event.class))).thenAnswer(invocation -> {
            Event toSave = invocation.getArgument(0);
            ReflectionTestUtils.setField(toSave, "id", 12L);
            ReflectionTestUtils.setField(toSave, "version", 0L);
            return toSave;
        });

        EventResponse result = eventService.create(ORG_ID, request(), CREATOR_ID);

        assertThat(result).isEqualTo(new EventResponse(12L, ORG_ID, "UBB Computer Science Society",
                "Intro to Docker", "Hands-on workshop", "FSEGA, room 2", EventCategory.WORKSHOP,
                START, END, 40, DEADLINE, EventStatus.DRAFT, 0, 40, 0L));
        ArgumentCaptor<Event> saved = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getOrganization()).isSameAs(organization);
        assertThat(saved.getValue().getCreatedBy()).isSameAs(creator);
        assertThat(saved.getValue().getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void createThrowsNotFoundForUnknownOrganizationAndSavesNothing() {
        when(organizationService.getEntityById(99L)).thenThrow(new NotFoundException("Organization 99 not found"));

        assertThatThrownBy(() -> eventService.create(99L, request(), CREATOR_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Organization 99 not found");
        verifyNoInteractions(eventRepository);
    }

    private static EventRequest request() {
        return new EventRequest("Intro to Docker", "Hands-on workshop", "FSEGA, room 2", EventCategory.WORKSHOP,
                START, END, 40, DEADLINE);
    }
}
