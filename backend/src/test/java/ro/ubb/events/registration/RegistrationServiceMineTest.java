package ro.ubb.events.registration;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.test.util.ReflectionTestUtils;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.service.EventService;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.service.OrgPermissionService;
import ro.ubb.events.registration.dto.MyRegistrationResponse;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.mapper.RegistrationMapper;
import ro.ubb.events.registration.repository.RegistrationRepository;
import ro.ubb.events.registration.service.RegistrationService;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Unit test of "my registrations": the repository is mocked, the mapper is real. */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceMineTest {

    private static final Long USER_ID = 10L;

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventService eventService;
    @Mock private OrgPermissionService orgPermissionService;
    @Mock private UserService userService;

    private RegistrationService registrationService;
    private User user;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(registrationRepository, eventService,
                orgPermissionService, new RegistrationMapper(), userService);
        user = new User("andrei@test.ro", "hash", "Andrei Toma", GlobalRole.STUDENT);
    }

    @Test
    void mapsRegistrationsWithTheirEventDetailsKeepingTheRepositoryOrder() {
        Instant start = Instant.parse("2026-12-10T16:00:00Z");
        Registration confirmed = registration(7L, event(2L, "Intro to Spring Boot Workshop", "Room 2/I",
                EventStatus.PUBLISHED, start), RegistrationStatus.CONFIRMED);
        Registration cancelled = registration(6L, event(3L, "Board Games Evening", "Room 1",
                EventStatus.CANCELLED, start.plus(Duration.ofDays(1))), RegistrationStatus.CANCELLED);
        when(registrationRepository.findMine(USER_ID)).thenReturn(List.of(confirmed, cancelled));

        List<MyRegistrationResponse> result = registrationService.listMine(USER_ID);

        assertThat(result).containsExactly(
                new MyRegistrationResponse(7L, 2L, "Intro to Spring Boot Workshop", start, "Room 2/I",
                        EventStatus.PUBLISHED, RegistrationStatus.CONFIRMED, confirmed.getRegisteredAt(), null),
                new MyRegistrationResponse(6L, 3L, "Board Games Evening", start.plus(Duration.ofDays(1)), "Room 1",
                        EventStatus.CANCELLED, RegistrationStatus.CANCELLED, cancelled.getRegisteredAt(), null));
        verifyNoInteractions(eventService, userService, orgPermissionService);
    }

    @Test
    void returnsAnEmptyListWhenTheUserHasNoRegistrations() {
        when(registrationRepository.findMine(USER_ID)).thenReturn(List.of());

        assertThat(registrationService.listMine(USER_ID)).isEmpty();
    }

    private Event event(Long id, String title, String location, EventStatus status, Instant start) {
        Organization organization = new Organization("UBB CS Society", null, null);
        Event event = new Event(organization, user, title, location, EventCategory.WORKSHOP,
                start, start.plus(Duration.ofHours(2)), 30);
        ReflectionTestUtils.setField(event, "id", id);
        event.setStatus(status);
        return event;
    }

    private Registration registration(Long id, Event event, RegistrationStatus status) {
        Registration registration = new Registration(event, user);
        registration.setStatus(status);
        ReflectionTestUtils.setField(registration, "id", id);
        ReflectionTestUtils.setField(registration, "registeredAt", Instant.parse("2026-11-06T10:00:00Z"));
        return registration;
    }
}
