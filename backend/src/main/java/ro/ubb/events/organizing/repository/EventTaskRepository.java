package ro.ubb.events.organizing.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.ubb.events.organizing.entity.EventTask;

public interface EventTaskRepository extends JpaRepository<EventTask, Long> {

    List<EventTask> findByEventIdOrderByDueAtAsc(Long eventId);

    List<EventTask> findByAssigneeId(Long membershipId);
}
