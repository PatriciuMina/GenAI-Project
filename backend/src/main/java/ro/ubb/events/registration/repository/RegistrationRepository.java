package ro.ubb.events.registration.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    Optional<Registration> findByEventIdAndUserIdAndStatus(Long eventId, Long userId, RegistrationStatus status);

    boolean existsByEventId(Long eventId);

    /** True if the user has (or had) a registration of any status for the event. */
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    List<Registration> findByEventIdAndStatus(Long eventId, RegistrationStatus status);

    List<Registration> findByUserIdOrderByRegisteredAtDesc(Long userId);

    /** A user's registrations, newest first, with the events fetched in the same query. */
    @Query("select r from Registration r join fetch r.event where r.user.id = :userId "
            + "order by r.registeredAt desc, r.id desc")
    List<Registration> findMine(@Param("userId") Long userId);

    /** Participant list of an event, users fetched in the same query, sorted by full name. */
    @Query("select r from Registration r join fetch r.user u where r.event.id = :eventId order by u.fullName, r.id")
    List<Registration> findParticipants(@Param("eventId") Long eventId);

    @Query("select r from Registration r join fetch r.user u "
            + "where r.event.id = :eventId and r.status = :status order by u.fullName, r.id")
    List<Registration> findParticipantsByStatus(@Param("eventId") Long eventId,
                                                @Param("status") RegistrationStatus status);
}
