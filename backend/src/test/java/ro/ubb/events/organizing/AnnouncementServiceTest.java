package ro.ubb.events.organizing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.organizing.dto.AnnouncementRequest;
import ro.ubb.events.organizing.dto.AnnouncementResponse;
import ro.ubb.events.organizing.entity.Announcement;
import ro.ubb.events.organizing.mapper.AnnouncementMapper;
import ro.ubb.events.organizing.repository.AnnouncementRepository;
import ro.ubb.events.organizing.service.AnnouncementService;
import ro.ubb.events.registration.service.RegistrationService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Unit test: repository and other features' services are mocked, the mapper is real. */
@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    private static final Long EVENT_ID = 5L;
    private static final Long ORG_ID = 2L;
    private static final Long CALLER_ID = 10L;

    @Mock private AnnouncementRepository announcementRepository;
    @Mock private EventService eventService;
    @Mock private UserService userService;
    @Mock private OrgPermissionService orgPermissionService;
    @Mock private RegistrationService registrationService;

    private AnnouncementService announcementService;
    private Event event;
    private User caller;

    @BeforeEach
    void setUp() {
        announcementService = new AnnouncementService(announcementRepository, eventService, userService,
                orgPermissionService, registrationService, new AnnouncementMapper());
        Organization organization = new Organization("UBB CS Society", null, null);
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        caller = new User("elena@test.ro", "hash", "Elena Marin", GlobalRole.STUDENT);
        Instant start = Instant.now().plus(Duration.ofDays(10));
        event = new Event(organization, caller, "Workshop", "FSEGA", EventCategory.WORKSHOP,
                start, start.plus(Duration.ofHours(2)), 30);
        event.setStatus(EventStatus.PUBLISHED);
    }

    // ---- list

    @Test
    void organizationMemberCanListWithoutARegistrationLookup() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isMember(CALLER_ID, ORG_ID)).thenReturn(true);
        when(announcementRepository.findByEventIdOrderByCreatedAtDesc(EVENT_ID))
                .thenReturn(List.of(announcement("Bring your laptop")));

        List<AnnouncementResponse> result = announcementService.list(EVENT_ID, CALLER_ID);

        assertThat(result).singleElement().satisfies(a -> {
            assertThat(a.title()).isEqualTo("Bring your laptop");
            assertThat(a.authorName()).isEqualTo("Elena Marin");
        });
        verifyNoInteractions(registrationService);
    }

    @Test
    void registeredNonMemberCanList() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isMember(CALLER_ID, ORG_ID)).thenReturn(false);
        when(registrationService.hasRegistration(EVENT_ID, CALLER_ID)).thenReturn(true);
        when(announcementRepository.findByEventIdOrderByCreatedAtDesc(EVENT_ID)).thenReturn(List.of());

        assertThat(announcementService.list(EVENT_ID, CALLER_ID)).isEmpty();
    }

    @Test
    void unrelatedUserCannotList() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isMember(CALLER_ID, ORG_ID)).thenReturn(false);
        when(registrationService.hasRegistration(EVENT_ID, CALLER_ID)).thenReturn(false);

        assertThatThrownBy(() -> announcementService.list(EVENT_ID, CALLER_ID))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(ex.getCode()).isEqualTo("FORBIDDEN");
                });
        verifyNoInteractions(announcementRepository);
    }

    @Test
    void listOfUnknownEventThrowsNotFound() {
        when(eventService.getEntityById(EVENT_ID)).thenThrow(new NotFoundException("Event 5 not found"));

        assertThatThrownBy(() -> announcementService.list(EVENT_ID, CALLER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    // ---- create

    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"PUBLISHED", "REGISTRATION_CLOSED", "ONGOING", "CANCELLED"})
    void organizerPostsAnnouncementAuthoredByTheCaller(EventStatus status) {
        event.setStatus(status);
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(true);
        when(userService.getEntityById(CALLER_ID)).thenReturn(caller);
        when(announcementRepository.saveAndFlush(any(Announcement.class))).thenAnswer(i -> i.getArgument(0));

        AnnouncementResponse result = announcementService.create(EVENT_ID,
                new AnnouncementRequest("  Room changed ", "We moved to room 5/I."), CALLER_ID);

        ArgumentCaptor<Announcement> saved = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getEvent()).isSameAs(event);
        assertThat(saved.getValue().getAuthor()).isSameAs(caller);
        assertThat(saved.getValue().getTitle()).isEqualTo("Room changed");
        assertThat(saved.getValue().getBody()).isEqualTo("We moved to room 5/I.");
        assertThat(result.authorName()).isEqualTo("Elena Marin");
    }

    @Test
    void nonOrganizerCannotPost() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(false);

        assertThatThrownBy(() -> announcementService.create(EVENT_ID, new AnnouncementRequest("t", "b"), CALLER_ID))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
        verify(announcementRepository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"DRAFT", "COMPLETED"})
    void cannotPostOnDraftOrCompletedEvents(EventStatus status) {
        event.setStatus(status);
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(true);

        assertThatThrownBy(() -> announcementService.create(EVENT_ID, new AnnouncementRequest("t", "b"), CALLER_ID))
                .isInstanceOfSatisfying(ConflictException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("ANNOUNCEMENT_NOT_ALLOWED"));
        verify(announcementRepository, never()).saveAndFlush(any());
    }

    @Test
    void postOnUnknownEventThrowsNotFound() {
        when(eventService.getEntityById(EVENT_ID)).thenThrow(new NotFoundException("Event 5 not found"));

        assertThatThrownBy(() -> announcementService.create(EVENT_ID, new AnnouncementRequest("t", "b"), CALLER_ID))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(orgPermissionService, announcementRepository);
    }

    private Announcement announcement(String title) {
        return new Announcement(event, caller, title, "Please install JDK 21.");
    }
}
