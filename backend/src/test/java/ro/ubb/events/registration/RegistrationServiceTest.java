package ro.ubb.events.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
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

/** Unit test for the participant list: repository and other features' services are mocked. */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final Long EVENT_ID = 5L;
    private static final Long ORG_ID = 2L;
    private static final Long CALLER_ID = 10L;

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventService eventService;
    @Mock private OrgPermissionService orgPermissionService;
    @Mock private UserService userService;

    private RegistrationService registrationService;
    private Event event;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(registrationRepository, eventService,
                orgPermissionService, new RegistrationMapper(), userService);
        Organization organization = new Organization("UBB CS Society", null, null);
        ReflectionTestUtils.setField(organization, "id", ORG_ID);
        User creator = new User("elena@test.ro", "hash", "Elena Marin", GlobalRole.STUDENT);
        Instant start = Instant.now().plus(Duration.ofDays(10));
        event = new Event(organization, creator, "Career Fair", "FSEGA", EventCategory.WORKSHOP,
                start, start.plus(Duration.ofHours(2)), 30);
    }

    @Test
    void organizerGetsAllParticipantsMappedToDtos() {
        Registration registration = registration(7L, 70L, "Andrei Toma", "andrei@test.ro");
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(true);
        when(registrationRepository.findParticipants(EVENT_ID)).thenReturn(List.of(registration));

        List<ParticipantResponse> result = registrationService.listParticipants(EVENT_ID, null, CALLER_ID);

        assertThat(result).containsExactly(new ParticipantResponse(7L, 70L, "Andrei Toma", "andrei@test.ro",
                RegistrationStatus.CONFIRMED, registration.getRegisteredAt(), null));
    }

    @Test
    void statusFilterUsesTheFilteredQuery() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(true);
        when(registrationRepository.findParticipantsByStatus(EVENT_ID, RegistrationStatus.CANCELLED))
                .thenReturn(List.of());

        List<ParticipantResponse> result =
                registrationService.listParticipants(EVENT_ID, RegistrationStatus.CANCELLED, CALLER_ID);

        assertThat(result).isEmpty();
        verify(registrationRepository).findParticipantsByStatus(EVENT_ID, RegistrationStatus.CANCELLED);
    }

    @Test
    void nonOrganizerGetsForbiddenAndNoPersonalDataIsLoaded() {
        when(eventService.getEntityById(EVENT_ID)).thenReturn(event);
        when(orgPermissionService.isOrganizer(CALLER_ID, ORG_ID)).thenReturn(false);

        assertThatThrownBy(() -> registrationService.listParticipants(EVENT_ID, null, CALLER_ID))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(ex.getCode()).isEqualTo("FORBIDDEN");
                });
        verifyNoInteractions(registrationRepository);
    }

    @Test
    void unknownEventThrowsNotFound() {
        when(eventService.getEntityById(EVENT_ID)).thenThrow(new NotFoundException("Event 5 not found"));

        assertThatThrownBy(() -> registrationService.listParticipants(EVENT_ID, null, CALLER_ID))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(orgPermissionService, registrationRepository);
    }

    private Registration registration(Long id, Long userId, String fullName, String email) {
        User user = new User(email, "hash", fullName, GlobalRole.STUDENT);
        ReflectionTestUtils.setField(user, "id", userId);
        Registration registration = new Registration(event, user);
        ReflectionTestUtils.setField(registration, "id", id);
        ReflectionTestUtils.setField(registration, "registeredAt", Instant.parse("2026-11-05T10:00:00Z"));
        return registration;
    }
}
