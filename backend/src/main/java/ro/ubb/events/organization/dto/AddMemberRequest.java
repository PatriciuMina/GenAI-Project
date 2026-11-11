package ro.ubb.events.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Body of {@code POST /organizations/{orgId}/members}: an existing account (by email) and its role.
 * {@code role} is a string on purpose: a value other than MEMBER/ORGANIZER (including PRESIDENT,
 * which only an admin sets when creating the organization) becomes a 400 with a field error on
 * {@code role}, instead of a generic "malformed request" from enum parsing.
 */
public record AddMemberRequest(
        @NotBlank @Email @Size(max = 255)
        @Schema(example = "andrei.toma@stud.ubbcluj.ro")
        String email,

        @NotBlank
        @Pattern(regexp = "MEMBER|ORGANIZER", message = "must be MEMBER or ORGANIZER")
        @Schema(allowableValues = {"MEMBER", "ORGANIZER"}, example = "MEMBER")
        String role) {
}
