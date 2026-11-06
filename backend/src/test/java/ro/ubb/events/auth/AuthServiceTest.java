package ro.ubb.events.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.ubb.events.auth.dto.LoginRequest;
import ro.ubb.events.auth.dto.LoginResponse;
import ro.ubb.events.auth.dto.RegisterRequest;
import ro.ubb.events.auth.service.AuthService;
import ro.ubb.events.auth.service.JwtService;
import ro.ubb.events.common.exception.ApiException;
import ro.ubb.events.user.entity.GlobalRole;
import ro.ubb.events.user.entity.User;
import ro.ubb.events.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private final User ana = new User("ana@test.ro", "hashed", "Ana", GlobalRole.STUDENT);

    @Test
    void registerNormalizesEmailAndStoresOnlyTheHash() {
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");

        authService.register(new RegisterRequest("  Ana@Test.RO ", "secret123", " Ana "));

        verify(userService).createStudent("ana@test.ro", "hashed", "Ana");
    }

    @Test
    void loginReturnsTokenForCorrectPassword() {
        when(userService.findByEmail("ana@test.ro")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(jwtService.issueToken(ana)).thenReturn("jwt");
        when(jwtService.expiresInSeconds()).thenReturn(7200L);

        LoginResponse response = authService.login(new LoginRequest("ana@test.ro", "secret123"));

        assertThat(response).isEqualTo(new LoginResponse("jwt", "Bearer", 7200L));
    }

    @Test
    void loginWithWrongPasswordFails() {
        when(userService.findByEmail("ana@test.ro")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertInvalidCredentials(new LoginRequest("ana@test.ro", "wrong"));
    }

    @Test
    void loginWithUnknownEmailFailsWithTheSameError() {
        when(userService.findByEmail("nobody@test.ro")).thenReturn(Optional.empty());

        assertInvalidCredentials(new LoginRequest("nobody@test.ro", "secret123"));
    }

    private void assertInvalidCredentials(LoginRequest request) {
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(e.getCode()).isEqualTo("INVALID_CREDENTIALS");
                    assertThat(e.getMessage()).isEqualTo("Invalid credentials");
                });
        verify(jwtService, never()).issueToken(any());
    }
}
