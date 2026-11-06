package ro.ubb.events.organizing.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.ubb.events.organizing.entity.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByEventIdOrderByCreatedAtDesc(Long eventId);
}
