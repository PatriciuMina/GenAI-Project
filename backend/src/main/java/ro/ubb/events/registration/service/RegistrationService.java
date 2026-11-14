package ro.ubb.events.registration.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.repository.RegistrationRepository;

/** Read operations used by other features go through this service, not the repository. */
@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;

    public RegistrationService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    @Transactional(readOnly = true)
    public long countConfirmedByEventId(Long eventId) {
        return registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED);
    }
}
