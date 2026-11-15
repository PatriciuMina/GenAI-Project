package ro.ubb.events.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.registration.dto.RegistrationResponse;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.mapper.RegistrationMapper;
import ro.ubb.events.registration.repository.RegistrationRepository;
import ro.ubb.events.registration.service.RegistrationService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Unit test of the register rules (W4), in the order the service applies them. */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceRegisterTest {

    private static final Long EVENT_ID = 5L;
    private static final Long USER_ID = 10L;
    private static final int CAPACITY = 30;

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventService eventService;
    @Mock private OrgPermissionService orgPermissionService;
    @Mock private UserService userService;

    private RegistrationService registrationService;
    private Event event;
    private User user;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(registrationRepository, eventService,
                orgPermissionService, new RegistrationMapper(), userService);
        Organization organization = new Organization("UBB CS Society", null, null);
        user = new User("andrei@test.ro", "hash", "Andrei Toma", GlobalRole.STUDENT);
        Instant start = Instant.now().plus(Duration.ofDays(10));
        event = new Event(organization, user, "Improv Night", "Studio", EventCategory.CULTURAL,
                start, start.plus(Duration.ofHours(2)), CAPACITY);
        ReflectionTestUtils.setField(event, "id", EVENT_ID);
        event.setStatus(EventStatus.PUBLISHED);
        event.setRegistrationDeadline(Instant.now().plus(Duration.ofDays(9)));
    }

    @Test
    void registersTheCallerAsConfirmed() {
        openRegistrationWithPlacesLeft();
        when(registrationRepository.saveAndFlush(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        RegistrationResponse response = registrationService.register(EVENT_ID, USER_ID);

        ArgumentCaptor<Registration> saved = ArgumentCaptor.forClass(Registration.class);
        verify(registrationRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getEvent()).isSameAs(event);
        assertThat(saved.getValue().getUser()).isSameAs(user);
        assertThat(saved.getValue().getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
        assertThat(response.eventId()).isEqualTo(EVENT_ID);
        assertThat(response.eventTitle()).isEqualTo("Improv Night");
        assertThat(response.status()).isEqualTo(RegistrationStatus.CONFIRMED);
        assertThat(response.checkedInAt()).isNull();
    }

    @Test
    void lastFreePlaceCanStillBeTaken() {
        when(eventService.getEntityByIdForUpdate(EVENT_ID)).thenReturn(event);
        when(registrationRepository.findByEventIdAndUserIdAndStatus(EVENT_ID, USER_ID, RegistrationStatus.CONFIRMED))
                .thenReturn(Optional.empty());
        when(registrationRepository.countByEventIdAndStatus(EVENT_ID, RegistrationStatus.CONFIRMED))
                .thenReturn((long) CAPACITY - 1);
        when(userService.getEntityById(USER_ID)).thenReturn(user);
        when(registrationRepository.saveAndFlush(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(registrationService.register(EVENT_ID, USER_ID).status()).isEqualTo(RegistrationStatus.CONFIRMED);
    }

    @Test
    void unknownEventThrowsNotFound() {
        when(eventService.getEntityByIdForUpdate(EVENT_ID)).thenThrow(new NotFoundException("Event 5 not found"));

        assertThatThrownBy(() -> registrationService.register(EVENT_ID, USER_ID))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(registrationRepository, userService);
    }

    @Test
    void draftEventIsHiddenAs404() {
        event.setStatus(EventStatus.DRAFT);
        when(eventService.getEntityByIdForUpdate(EVENT_ID)).thenReturn(event);

        assertThatThrownBy(() -> registrationService.register(EVENT_ID, USER_ID))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(registrationRepository, userService);
    }

    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"REGISTRATION_CLOSED", "ONGOING", "COMPLETED", "CANCELLED"})
    void onlyPublishedEventsAcceptRegistrations(EventStatus status) {
        event.setStatus(status);
        when(eventService.getEntityByIdForUpdate(EVENT_ID)).thenReturn(event);

        assertConflict("REGISTRATION_CLOSED");
        verifyNoInteractions(registrationRepository, userService);
    }

    @Test
    void closedAfterTheRegistrationDeadline() {
        event.setRegistrationDeadline(Instant.now().minus(Duration.ofMinutes(1)));
        when(eventService.getEntityByIdForUpdate(EVENT_ID)).thenReturn(event);

        assertConflict("REGISTRATION_CLOSED");
        verifyNoInteractions(registrationRepository, userService);
    }

    @Test
    void withoutADeadlineTheStartOfTheEventIsTheLimit() {
        event.setRegistrationDeadline(null);
        ReflectionTestUtils.setField(event, "startAt", Instant.now().minus(Duration.ofMinutes(1)));
        when(eventService.getEntityByIdForUpdate(EVENT_ID)).thenReturn(event);

        assertConflict("REGISTRATION_CLOSED");
    }

    @Test
    void withoutADeadlineRegistrationIsOpenUntilTheStart() {
        event.setRegistrationDeadline(null);
        openRegistrationWithPlacesLeft();
        when(registrationRepository.saveAndFlush(any(Registration.class))).thenAnswer(i -> i.getArgument(0));

        assertThat(registrationService.register(EVENT_ID, USER_ID).status()).isEqualTo(RegistrationStatus.CONFIRMED);
    }

    @Test
    void alreadyConfirmedCallerGetsAlreadyRegisteredAndCapacityIsNotChecked() {
        when(eventService.getEntityByIdForUpdate(EVENT_ID)).thenReturn(event);
        when(registrationRepository.findByEventIdAndUserIdAndStatus(EVENT_ID, USER_ID, RegistrationStatus.CONFIRMED))
                .thenReturn(Optional.of(new Registration(event, user)));

        assertConflict("ALREADY_REGISTERED");
        verify(registrationRepository, never()).countByEventIdAndStatus(any(), any());
        verify(registrationRepository, never()).saveAndFlush(any());
    }

    @Test
    void fullEventGetsEventFull() {
        when(eventService.getEntityByIdForUpdate(EVENT_ID)).thenReturn(event);
        when(registrationRepository.findByEventIdAndUserIdAndStatus(EVENT_ID, USER_ID, RegistrationStatus.CONFIRMED))
                .thenReturn(Optional.empty());
        when(registrationRepository.countByEventIdAndStatus(EVENT_ID, RegistrationStatus.CONFIRMED))
                .thenReturn((long) CAPACITY);

        assertConflict("EVENT_FULL");
        verify(registrationRepository, never()).saveAndFlush(any());
    }

    @Test
    void duplicateRejectedByTheUniqueIndexIsMappedToAlreadyRegistered() {
        openRegistrationWithPlacesLeft();
        when(registrationRepository.saveAndFlush(any(Registration.class)))
                .thenThrow(new DataIntegrityViolationException("ux_registrations_confirmed_once"));

        assertConflict("ALREADY_REGISTERED");
    }

    private void openRegistrationWithPlacesLeft() {
        when(eventService.getEntityByIdForUpdate(EVENT_ID)).thenReturn(event);
        when(registrationRepository.findByEventIdAndUserIdAndStatus(EVENT_ID, USER_ID, RegistrationStatus.CONFIRMED))
                .thenReturn(Optional.empty());
        when(registrationRepository.countByEventIdAndStatus(EVENT_ID, RegistrationStatus.CONFIRMED)).thenReturn(0L);
        when(userService.getEntityById(USER_ID)).thenReturn(user);
    }

    private void assertConflict(String code) {
        assertThatThrownBy(() -> registrationService.register(EVENT_ID, USER_ID))
                .isInstanceOfSatisfying(ConflictException.class, ex -> assertThat(ex.getCode()).isEqualTo(code));
    }
}
