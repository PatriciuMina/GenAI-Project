package ro.ubb.events.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.mapper.RegistrationMapper;
import ro.ubb.events.registration.repository.RegistrationRepository;
import ro.ubb.events.registration.service.RegistrationService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceCancelTest {

    private static final Long EVENT_ID = 5L;
    private static final Long USER_ID = 10L;

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
        Organization organization = new Organization("UBB CS Society", null, null);
        User user = new User("andrei@test.ro", "hash", "Andrei Toma", GlobalRole.STUDENT);
        Instant start = Instant.now().plus(Duration.ofDays(10));
        event = new Event(organization, user, "Intro to Spring Boot Workshop", "FSEGA", EventCategory.WORKSHOP,
                start, start.plus(Duration.ofHours(2)), 30);
        event.setStatus(EventStatus.PUBLISHED);
        registration = new Registration(event, user);
    }

    @Test
    void cancelsTheCallersConfirmedRegistrationAndKeepsTheRow() {
        callerHasConfirmedRegistration();

        registrationService.cancelMyRegistration(EVENT_ID, USER_ID);

        assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
        verify(registrationRepository).saveAndFlush(registration);
        verify(registrationRepository, never()).delete(any());
    }

    @Test
    void cancellingIsAllowedAfterRegistrationClosedAsLongAsTheEventHasNotStarted() {
        event.setStatus(EventStatus.REGISTRATION_CLOSED);
        callerHasConfirmedRegistration();

        registrationService.cancelMyRegistration(EVENT_ID, USER_ID);

        assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
    }

    @Test
    void withoutAConfirmedRegistrationThrowsNotRegistered() {
        when(registrationRepository.findByEventIdAndUserIdAndStatus(EVENT_ID, USER_ID, RegistrationStatus.CONFIRMED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrationService.cancelMyRegistration(EVENT_ID, USER_ID))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(e.getCode()).isEqualTo("NOT_REGISTERED");
                });
        verify(registrationRepository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"DRAFT", "ONGOING", "COMPLETED", "CANCELLED"})
    void eventsThatAreNotOpenCannotBeCancelled(EventStatus status) {
        event.setStatus(status);
        callerHasConfirmedRegistration();

        assertCancelNotAllowed();
    }

    @Test
    void anEventThatAlreadyStartedCannotBeCancelledEvenIfStillPublished() {
        event.setStartAt(Instant.now().minus(Duration.ofMinutes(1)));
        callerHasConfirmedRegistration();

        assertCancelNotAllowed();
    }

    @Test
    void aCheckedInRegistrationCannotBeCancelled() {
        event.setStatus(EventStatus.REGISTRATION_CLOSED);
        registration.checkIn(Instant.now());
        callerHasConfirmedRegistration();

        assertCancelNotAllowed();
    }

    private void callerHasConfirmedRegistration() {
        when(registrationRepository.findByEventIdAndUserIdAndStatus(EVENT_ID, USER_ID, RegistrationStatus.CONFIRMED))
                .thenReturn(Optional.of(registration));
    }

    private void assertCancelNotAllowed() {
        assertThatThrownBy(() -> registrationService.cancelMyRegistration(EVENT_ID, USER_ID))
                .isInstanceOfSatisfying(ConflictException.class,
                        e -> assertThat(e.getCode()).isEqualTo("CANCEL_NOT_ALLOWED"));
        assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
        verify(registrationRepository, never()).saveAndFlush(any());
    }
}
