package ro.ubb.events.user.service;

import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.common.exception.ConflictException;
import ro.ubb.events.common.exception.NotFoundException;
import ro.ubb.events.user.dto.UserResponse;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.repository.UserRepository;

/** User accounts. Other features (e.g. auth) use this service, never {@link UserRepository} directly. */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Creates a STUDENT. 409 EMAIL_ALREADY_USED if the email exists (case-insensitive). */
    @Transactional
    public UserResponse createStudent(String email, String passwordHash, String fullName) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw emailAlreadyUsed();
        }
        try {
            User saved = userRepository.saveAndFlush(new User(email, passwordHash, fullName, GlobalRole.STUDENT));
            return toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            // two registrations with the same email at the same time: the unique index wins
            throw emailAlreadyUsed();
        }
    }

    /** For the service layer only (the entity must not reach controllers). */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(UserService::toResponse)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }

    private static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getGlobalRole(),
                user.getCreatedAt());
    }

    private static ConflictException emailAlreadyUsed() {
        return new ConflictException("EMAIL_ALREADY_USED", "An account with this email already exists");
    }
}
