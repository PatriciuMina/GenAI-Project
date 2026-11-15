package ro.ubb.events.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.ubb.events.auth.dto.AuthenticatedUser;
import ro.ubb.events.common.dto.ApiError;
import ro.ubb.events.registration.dto.ParticipantResponse;
import ro.ubb.events.registration.entity.RegistrationStatus;
import ro.ubb.events.registration.service.RegistrationService;

/** Registrations of events (W4 participant registration, W5 participant list). */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Registrations", description = "Event registrations (W4, W5)")
@ApiResponse(responseCode = "401", description = "Missing, invalid or expired token",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/events/{eventId}/registrations")
    @Operation(summary = "List the participants of an event (organizer or president of its organization)",
            description = "Sorted by full name. Optional status filter (CONFIRMED or CANCELLED).")
    @ApiResponse(responseCode = "200", description = "Participants of the event")
    @ApiResponse(responseCode = "403", description = "Not an organizer or president of the event's organization",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<ParticipantResponse> listParticipants(
            @PathVariable Long eventId,
            @RequestParam(required = false) RegistrationStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return registrationService.listParticipants(eventId, status, me.id());
    }
}
