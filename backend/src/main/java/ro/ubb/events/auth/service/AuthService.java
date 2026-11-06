package ro.ubb.events.auth.service;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubb.events.auth.dto.LoginRequest;
import ro.ubb.events.auth.dto.LoginResponse;
import ro.ubb.events.auth.dto.RegisterRequest;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.user.dto.UserResponse;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

/** Registration and login. Users are stored through {@link UserService} (cross-feature via service). */
@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Creates a STUDENT account. 409 EMAIL_ALREADY_USED if the email exists (case-insensitive). */
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String passwordHash = passwordEncoder.encode(request.password());
        return userService.createStudent(email, passwordHash, request.fullName().trim());
    }

    /** Returns a token, or 401 INVALID_CREDENTIALS (same message for unknown email and wrong password). */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userService.findByEmail(request.email().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials"));
        return new LoginResponse(jwtService.issueToken(user), "Bearer", jwtService.expiresInSeconds());
    }
}
