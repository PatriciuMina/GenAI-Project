package ro.ubb.events.user.dto;

import java.time.Instant;
import ro.ubb.events.user.entity.GlobalRole;

/** Public view of a user account (never contains the password hash). */
public record UserResponse(Long id, String email, String fullName, GlobalRole role, Instant createdAt) {
}
