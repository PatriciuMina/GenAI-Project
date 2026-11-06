package ro.ubb.events.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import ro.ubb.events.auth.dto.AuthenticatedUser;
import ro.ubb.events.common.dto.ApiError;
import ro.ubb.events.user.entity.GlobalRole;

/**
 * Stateless JWT security (ADR-003).
 * <ul>
 *   <li>Public: health, Swagger/OpenAPI, {@code POST /api/v1/auth/register} and {@code /login}.</li>
 *   <li>Everything else needs a valid {@code Authorization: Bearer <token>} header.</li>
 *   <li>Spring's OAuth2 resource server validates the token (HS256 signature + expiry);
 *       {@link #toAuthentication(Jwt)} turns it into an {@link AuthenticatedUser} principal
 *       with authority {@code ROLE_STUDENT} or {@code ROLE_ADMIN}.</li>
 *   <li>{@code @EnableMethodSecurity} enables {@code @PreAuthorize} on controllers/services.</li>
 * </ul>
 * Errors use the common {@link ApiError} body: 401 for missing/invalid/expired token,
 * 403 for an authenticated user without permission.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/health",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    private static final String[] PUBLIC_POST_PATHS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login"
    };

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint unauthorized = (req, res, e) -> {
            res.setHeader("WWW-Authenticate", "Bearer");
            writeError(res, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing, invalid or expired token");
        };
        AccessDeniedHandler forbidden = (req, res, e) ->
                writeError(res, HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied");

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_PATHS).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(SecurityConfig::toAuthentication))
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler(forbidden))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler(forbidden))
                .build();
    }

    /** BCrypt with the default strength 10 (same as the dev seed hashes). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /** Signs tokens on login (used by {@code auth.service.JwtService}). */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    /** Verifies the signature and the {@code exp} claim of incoming tokens. */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /** Builds the principal from the claims written by {@code JwtService}: sub = user id, email, role. */
    static AbstractAuthenticationToken toAuthentication(Jwt jwt) {
        AuthenticatedUser user;
        try {
            user = new AuthenticatedUser(
                    Long.valueOf(jwt.getSubject()),
                    jwt.getClaimAsString("email"),
                    GlobalRole.valueOf(jwt.getClaimAsString("role")));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidBearerTokenException("Token has invalid claims");
        }
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));
        return new UsernamePasswordAuthenticationToken(user, jwt, authorities);
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiError.of(status.value(), code, message));
    }
}
