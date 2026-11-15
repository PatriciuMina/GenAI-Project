package ro.ubb.events.organizing.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.ubb.events.organizing.entity.EventTask;

public interface EventTaskRepository extends JpaRepository<EventTask, Long> {

    List<EventTask> findByEventIdOrderByDueAtAsc(Long eventId);

    List<EventTask> findByAssigneeId(Long membershipId);

    /** A task of this event; empty when the id belongs to a task of another event or does not exist. */
    Optional<EventTask> findByIdAndEventId(Long id, Long eventId);

    /**
     * Tasks of an event by due date (tasks without a due date last), the assignee and their user fetched in
     * the same query so mapping to a DTO needs no extra query per row.
     */
    @Query("select t from EventTask t left join fetch t.assignee a left join fetch a.user "
            + "where t.event.id = :eventId order by t.dueAt asc nulls last, t.id asc")
    List<EventTask> findByEventIdWithAssignee(@Param("eventId") Long eventId);
}
