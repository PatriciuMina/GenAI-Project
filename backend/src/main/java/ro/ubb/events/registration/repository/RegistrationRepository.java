package ro.ubb.events.registration.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.ubb.events.registration.entity.Registration;
import ro.ubb.events.registration.entity.RegistrationStatus;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);

    Optional<Registration> findByEventIdAndUserIdAndStatus(Long eventId, Long userId, RegistrationStatus status);

    boolean existsByEventId(Long eventId);

    List<Registration> findByEventIdAndStatus(Long eventId, RegistrationStatus status);

    List<Registration> findByUserIdOrderByRegisteredAtDesc(Long userId);
}
