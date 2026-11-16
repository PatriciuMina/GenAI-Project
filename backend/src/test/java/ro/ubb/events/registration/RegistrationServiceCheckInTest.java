package ro.ubb.events.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import ro.ubb.events.registration.dto.ParticipantResponse;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.mapper.RegistrationMapper;
import ro.ubb.events.registration.repository.RegistrationRepository;
import ro.ubb.events.registration.service.RegistrationService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Unit test for the check-in rules: repository and other features' services are mocked. */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceCheckInTest {

    private static final Long EVENT_ID = 5L;
    private static final Long ORG_ID = 2L;
    private static final Long CALLER_ID = 10L;
    private static final Long REGISTRATION_ID = 7L;

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventService eventService;
    @Mock private OrgPermissionService orgPermissionService;
    @Mock private UserService userService;

    private RegistrationService registrationService;
    private Event event;
    private Registration registration;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(registrationRepository, eventService,
                orgPermissionService, new RegistrationMapper(), userService);
        Organization organization = new Organization("Theatre Club", null, null);
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        User creator = new User("radu@test.ro", "hash", "Radu Pop", GlobalRole.STUDENT);
        Instant start = Instant.now().plus(Duration.ofDays(1));
        event = new Event(organization, creator, "Reading Marathon", "Hall", EventCategory.OTHER,
                start, start.plus(Duration.ofHours(8)), 30);
        event.setStatus(EventStatus.ONGOING);
        User participant = new User("maria@test.ro", "hash", "Maria Dobre", GlobalRole.STUDENT);
        registration = new Registration(event, participant);
        ReflectionTestUtils.setField(registration, "id", REGISTRATION_ID);
    }

    private void callerIsOrganizer(boolean organizer) {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(organizer);
    }

    private void registrationFound() {
        when(registrationRepository.findByIdAndEventId(REGISTRATION_ID, EVENT_ID))
                .thenReturn(Optional.of(registration));
    }

    private void assertConflict(String code) {
        assertThatThrownBy(() -> registrationService.checkIn(EVENT_ID, REGISTRATION_ID, CALLER_ID))
                .isInstanceOfSatisfying(ConflictException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getCode()).isEqualTo(code);
                });
        assertThat(registration.getCheckedInAt()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"REGISTRATION_CLOSED", "ONGOING"})
    void organizerChecksInConfirmedParticipantWhenEventAllowsIt(EventStatus status) {
        event.setStatus(status);
        callerIsOrganizer(true);
        registrationFound();
        when(registrationRepository.saveAndFlush(registration)).thenReturn(registration);
        Instant before = Instant.now();

        ParticipantResponse result = registrationService.checkIn(EVENT_ID, REGISTRATION_ID, CALLER_ID);

        assertThat(result.checkedInAt()).isBetween(before, Instant.now());
        assertThat(registration.getCheckedInAt()).isEqualTo(result.checkedInAt());
        verify(registrationRepository).saveAndFlush(registration);
    }

    @Test
    void nonOrganizerGetsForbiddenAndRegistrationIsNotLoaded() {
        callerIsOrganizer(false);

        assertThatThrownBy(() -> registrationService.checkIn(EVENT_ID, REGISTRATION_ID, CALLER_ID))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
        verifyNoInteractions(registrationRepository);
    }

    @Test
    void unknownEventThrowsNotFound() {
        when(eventService.getEntityById(EVENT_ID)).thenThrow(new NotFoundException("Event 5 not found"));

        assertThatThrownBy(() -> registrationService.checkIn(EVENT_ID, REGISTRATION_ID, CALLER_ID))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(orgPermissionService, registrationRepository);
    }

    @Test
    void registrationNotFoundOrOfAnotherEventThrowsNotFound() {
        callerIsOrganizer(true);
        when(registrationRepository.findByIdAndEventId(REGISTRATION_ID, EVENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.checkIn(EVENT_ID, REGISTRATION_ID, CALLER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"REGISTRATION_CLOSED", "ONGOING"}, mode = EnumSource.Mode.EXCLUDE)
    void eventInAnyOtherStatusIsNotAllowed(EventStatus status) {
        event.setStatus(status);
        callerIsOrganizer(true);
        registrationFound();

        assertConflict("CHECK_IN_NOT_ALLOWED");
    }

    @Test
    void cancelledRegistrationIsNotAllowed() {
        registration.setStatus(RegistrationStatus.CANCELLED);
        callerIsOrganizer(true);
        registrationFound();

        assertConflict("CHECK_IN_NOT_ALLOWED");
    }

    @Test
    void secondCheckInIsRejectedAndKeepsTheFirstTimestamp() {
        Instant first = Instant.parse("2026-11-05T10:00:00Z");
        registration.checkIn(first);
        callerIsOrganizer(true);
        registrationFound();

        assertThatThrownBy(() -> registrationService.checkIn(EVENT_ID, REGISTRATION_ID, CALLER_ID))
                .isInstanceOfSatisfying(ConflictException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("ALREADY_CHECKED_IN"));
        assertThat(registration.getCheckedInAt()).isEqualTo(first);
    }
}
