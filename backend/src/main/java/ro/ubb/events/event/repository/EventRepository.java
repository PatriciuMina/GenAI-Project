package ro.ubb.events.event.repository;

import java.time.Instant;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.ubb.events.event.entity.Event;
import ro.ubb.events.event.entity.EventStatus;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /** Reads the event with a row lock (SELECT ... FOR UPDATE) held until the surrounding transaction ends. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    List<Event> findByOrganizationIdOrderByStartAtAsc(Long organizationId);

    Page<Event> findByStatusAndStartAtGreaterThanEqual(EventStatus status, Instant from, Pageable pageable);
}
