package ro.ubb.events.registration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.entity.EventStatus;
import ro.ubb.events.event.repository.EventRepository;
import ro.ubb.events.organization.entity.Organization;
import ro.ubb.events.organization.repository.OrganizationRepository;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.repository.RegistrationRepository;
import ro.ubb.events.registration.service.RegistrationService;
import ro.ubb.events.support.IntegrationTestBase;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;

/**
 * The capacity rule under concurrency (docs/data-model.md, rule 1): when many students try to take the
 * last places at the same moment, exactly as many registrations succeed as there are places.
 *
 * <p>Every attempt calls {@link RegistrationService#register} on its own thread, so each one runs in its
 * own database transaction (not inside one test transaction), against the real PostgreSQL container. A
 * latch releases all threads together to make them overlap.
 *
 * <p><b>Why this test would fail without the lock.</b> {@code register} first locks the event row
 * ({@code SELECT ... FOR UPDATE}) and only then counts the confirmed registrations and inserts. Without
 * that lock, under PostgreSQL's default READ COMMITTED isolation every transaction would count the
 * registrations before any other one has committed, so all of them would see a free place and all of them
 * would insert: more registrations than the capacity. The unique index on (event, user) does not help,
 * because the competing registrations are for different users. The lock makes the transactions go one
 * after the other, so each one sees the result of the previous one.
 */
class RegistrationConcurrencyTest extends IntegrationTestBase {

    private static final String OK = "REGISTERED";

    @Autowired private RegistrationService registrationService;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;

    @Test
    void capacityOneWithTenParallelAttemptsRegistersExactlyOneStudent() throws Exception {
        runParallelRegistrations(1, 10);
    }

    @Test
    void capacityFiveWithTwentyParallelAttemptsRegistersExactlyFiveStudents() throws Exception {
        runParallelRegistrations(5, 20);
    }

    private void runParallelRegistrations(int capacity, int attempts) throws Exception {
        Event event = publishedEvent(capacity);
        List<User> students = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            students.add(createUser(uniqueEmail("racer"), GlobalRole.STUDENT));
        }

        ExecutorService pool = Executors.newFixedThreadPool(attempts);
        CountDownLatch allReady = new CountDownLatch(attempts);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<String>> outcomes = new ArrayList<>();
        try {
            for (User student : students) {
                Callable<String> attempt = () -> {
                    allReady.countDown();
                    go.await();
                    try {
                        registrationService.register(event.getId(), student.getId());
                        return OK;
                    } catch (ConflictException ex) {
                        return ex.getCode();
                    }
                };
                outcomes.add(pool.submit(attempt));
            }
            assertThat(allReady.await(30, TimeUnit.SECONDS)).as("all threads started").isTrue();
            go.countDown();

            Map<String, Long> count = new HashMap<>();
            for (Future<String> outcome : outcomes) {
                count.merge(outcome.get(60, TimeUnit.SECONDS), 1L, Long::sum);
            }

            assertThat(count)
                    .as("outcomes of %d parallel attempts for %d places", attempts, capacity)
                    .containsOnlyKeys(OK, "EVENT_FULL")
                    .containsEntry(OK, (long) capacity)
                    .containsEntry("EVENT_FULL", (long) (attempts - capacity));
        } finally {
            pool.shutdownNow();
        }
        assertThat(registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.CONFIRMED))
                .as("CONFIRMED registrations in the database")
                .isEqualTo(capacity);
    }

    private Event publishedEvent(int capacity) {
        Organization organization = organizationRepository.save(new Organization("Org " + UUID.randomUUID(), null, null));
        User creator = createUser(uniqueEmail("creator"), GlobalRole.STUDENT);
        Instant start = Instant.now().plus(Duration.ofDays(10));
        Event event = new Event(organization, creator, "Race " + UUID.randomUUID(), "FSEGA",
                EventCategory.WORKSHOP, start, start.plus(Duration.ofHours(2)), capacity);
        event.setStatus(EventStatus.PUBLISHED);
        event.setRegistrationDeadline(Instant.now().plus(Duration.ofDays(9)));
        return eventRepository.save(event);
    }
}
