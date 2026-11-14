package ro.ubb.events.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.common.exception.ValidationException;
import ro.ubb.events.event.dto.EventRequest;
import ro.ubb.events.event.dto.EventResponse;
import ro.ubb.events.event.dto.UpdateEventRequest;
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
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.registration.service.RegistrationService;

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
    @Mock private OrgPermissionService orgPermissionService;
    @Mock private RegistrationService registrationService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, organizationService, userService, new EventMapper(),
                orgPermissionService, registrationService);
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

    @Test
    void getByIdReturnsEventWithRegistrationCounts() {
        Event event = existingEvent(EventStatus.PUBLISHED, 0L);
        when(eventRepository.findById(12L)).thenReturn(Optional.of(event));
        when(registrationService.countConfirmedByEventId(12L)).thenReturn(2L);

        EventResponse result = eventService.getById(12L, CREATOR_ID);

        assertThat(result.confirmedCount()).isEqualTo(2);
        assertThat(result.freePlaces()).isEqualTo(38);
        assertThat(result.status()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void getByIdHidesDraftFromNonOrganizer() {
        when(eventRepository.findById(12L)).thenReturn(Optional.of(existingEvent(EventStatus.DRAFT, 0L)));
        when(orgPermissionService.isOrganizer(CREATOR_ID, ORG_ID)).thenReturn(false);

        assertThatThrownBy(() -> eventService.getById(12L, CREATOR_ID))
                .isInstanceOf(NotFoundException.class).hasMessage("Event 12 not found");
        verifyNoInteractions(registrationService);
    }

    @Test
    void getByIdReturnsDraftToOrganizer() {
        when(eventRepository.findById(12L)).thenReturn(Optional.of(existingEvent(EventStatus.DRAFT, 0L)));
        when(orgPermissionService.isOrganizer(CREATOR_ID, ORG_ID)).thenReturn(true);
        when(registrationService.countConfirmedByEventId(12L)).thenReturn(0L);

        assertThat(eventService.getById(12L, CREATOR_ID).status()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void getByIdThrowsNotFoundForUnknownEvent() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getById(99L, CREATOR_ID))
                .isInstanceOf(NotFoundException.class).hasMessage("Event 99 not found");
    }

    @Test
    void updateChangesDraftAndIncrementsVersion() {
        Event event = existingEvent(EventStatus.DRAFT, 0L);
        when(eventRepository.findById(12L)).thenReturn(Optional.of(event));
        when(orgPermissionService.isOrganizer(CREATOR_ID, ORG_ID)).thenReturn(true);
        when(registrationService.countConfirmedByEventId(12L)).thenReturn(0L);

        EventResponse result = eventService.update(12L, updateRequest(0L, 35), CREATOR_ID);

        assertThat(result.title()).isEqualTo("Updated title");
        assertThat(result.capacity()).isEqualTo(35);
        verify(eventRepository).flush();
    }

    @Test
    void updateAllowsPublishedEvent() {
        Event event = existingEvent(EventStatus.PUBLISHED, 0L);
        when(eventRepository.findById(12L)).thenReturn(Optional.of(event));
        when(orgPermissionService.isOrganizer(CREATOR_ID, ORG_ID)).thenReturn(true);
        when(registrationService.countConfirmedByEventId(12L)).thenReturn(0L);

        assertThat(eventService.update(12L, updateRequest(0L, 35), CREATOR_ID).status())
                .isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void updateRejectsNonEditableStatusAndOldVersion() {
        Event completed = existingEvent(EventStatus.COMPLETED, 0L);
        when(eventRepository.findById(12L)).thenReturn(Optional.of(completed));
        when(orgPermissionService.isOrganizer(CREATOR_ID, ORG_ID)).thenReturn(true);
        assertThatThrownBy(() -> eventService.update(12L, updateRequest(0L, 35), CREATOR_ID))
                .isInstanceOf(ConflictException.class).hasMessageContaining("DRAFT or PUBLISHED");

        Event draft = existingEvent(EventStatus.DRAFT, 2L);
        when(eventRepository.findById(12L)).thenReturn(Optional.of(draft));
        assertThatThrownBy(() -> eventService.update(12L, updateRequest(1L, 35), CREATOR_ID))
                .isInstanceOf(ConflictException.class).hasMessageContaining("changed");
    }

    @Test
    void updateRejectsCapacityBelowConfirmedCount() {
        Event event = existingEvent(EventStatus.DRAFT, 0L);
        when(eventRepository.findById(12L)).thenReturn(Optional.of(event));
        when(orgPermissionService.isOrganizer(CREATOR_ID, ORG_ID)).thenReturn(true);
        when(registrationService.countConfirmedByEventId(12L)).thenReturn(2L);
        assertThatThrownBy(() -> eventService.update(12L, updateRequest(0L, 1), CREATOR_ID))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateRejectsNonOrganizer() {
        Event event = existingEvent(EventStatus.DRAFT, 0L);
        when(eventRepository.findById(12L)).thenReturn(Optional.of(event));
        when(orgPermissionService.isOrganizer(CREATOR_ID, ORG_ID)).thenReturn(false);
        assertThatThrownBy(() -> eventService.update(12L, updateRequest(0L, 35), CREATOR_ID))
                .isInstanceOf(ro.ubb.events.common.exception.ApiException.class)
                .hasMessageContaining("Not an organizer");
        verifyNoInteractions(registrationService);
    }

    private Event existingEvent(EventStatus status, long version) {
        Organization organization = new Organization("Test Org", null, null);
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        User creator = new User("creator@test.ro", "hash", "Creator", GlobalRole.STUDENT);
        Event event = new Event(organization, creator, "Old title", "Hall", EventCategory.WORKSHOP,
                START, END, 40);
        ReflectionTestUtils.setField(event, "id", 12L);
        ReflectionTestUtils.setField(event, "version", version);
        event.setStatus(status);
        return event;
    }

    private static UpdateEventRequest updateRequest(long version, int capacity) {
        return new UpdateEventRequest("Updated title", "New description", "Hall", EventCategory.WORKSHOP,
                START, END, capacity, DEADLINE, version);
    }

    private static EventRequest request() {
        return new EventRequest("Intro to Docker", "Hands-on workshop", "FSEGA, room 2", EventCategory.WORKSHOP,
                START, END, 40, DEADLINE);
    }
}
