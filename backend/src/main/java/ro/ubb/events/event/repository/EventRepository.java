package ro.ubb.events.event.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizationIdOrderByStartAtAsc(Long organizationId);

    Page<Event> findByStatusAndStartAtGreaterThanEqual(EventStatus status, Instant from, Pageable pageable);
}
