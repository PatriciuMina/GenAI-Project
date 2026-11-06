package ro.ubb.events.auth.dto;

import ro.ubb.events.user.entity.GlobalRole;

/**
 * The logged-in user, built from the JWT on every request (see {@code SecurityConfig}).
 * <ul>
 *   <li>In a controller: {@code @AuthenticationPrincipal AuthenticatedUser me}</li>
 *   <li>In {@code @PreAuthorize}: {@code principal.id}, {@code principal.role}</li>
 * </ul>
 */
public record AuthenticatedUser(Long id, String email, GlobalRole role) {
}
