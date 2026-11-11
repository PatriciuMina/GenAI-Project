package ro.ubb.events.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body of {@code POST /organizations}: a new organization and the account that becomes its president. */
public record CreateOrganizationRequest(
        @NotBlank @Size(max = 150)
        @Schema(example = "UBB Robotics Club")
        String name,

        @Schema(example = "Robots and coffee", nullable = true)
        String description,

        @Email @Size(max = 255)
        @Schema(example = "robotics@ubbcluj.ro", nullable = true)
        String contactEmail,

        @NotBlank @Email @Size(max = 255)
        @Schema(example = "andrei.toma@stud.ubbcluj.ro", description = "An existing account; becomes PRESIDENT")
        String presidentEmail) {
}
