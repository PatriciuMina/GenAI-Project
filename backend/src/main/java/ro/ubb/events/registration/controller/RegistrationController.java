package ro.ubb.events.registration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.ubb.events.auth.dto.AuthenticatedUser;
import ro.ubb.events.common.dto.ApiError;
import ro.ubb.events.registration.dto.MyRegistrationResponse;
import ro.ubb.events.registration.dto.ParticipantResponse;
import ro.ubb.events.registration.dto.RegistrationResponse;
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

    @PostMapping("/events/{eventId}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register the caller for a published event (capacity limit, no waitlist)",
            description = "The event must be PUBLISHED and the registration deadline (or the start, if there is "
                    + "none) not yet passed. A student who cancelled earlier may register again.")
    @ApiResponse(responseCode = "201", description = "Registered; the new CONFIRMED registration")
    @ApiResponse(responseCode = "404", description = "Event not found or still a DRAFT",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "REGISTRATION_CLOSED, EVENT_FULL or ALREADY_REGISTERED",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public RegistrationResponse register(
            @PathVariable Long eventId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return registrationService.register(eventId, me.id());
    }

    @DeleteMapping("/events/{eventId}/registrations/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel my registration for an event",
            description = "Frees the place. Allowed while the event is PUBLISHED or REGISTRATION_CLOSED and has "
                    + "not started, and only if not checked in. The registration is kept as CANCELLED, so the "
                    + "caller may register again later.")
    @ApiResponse(responseCode = "204", description = "Registration cancelled")
    @ApiResponse(responseCode = "404", description = "NOT_REGISTERED: no CONFIRMED registration of the caller",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "CANCEL_NOT_ALLOWED: event started, finished or cancelled, "
            + "or the caller is already checked in",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public void cancelMine(
            @PathVariable Long eventId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        registrationService.cancelMyRegistration(eventId, me.id());
    }

    @PostMapping("/events/{eventId}/registrations/{registrationId}/check-in")
    @Operation(summary = "Check a participant in at the door (organizer or president of the organization)",
            description = "The event must be REGISTRATION_CLOSED or ONGOING and the registration CONFIRMED. "
                    + "No undo in the MVP.")
    @ApiResponse(responseCode = "200", description = "Checked in; the participant with checkedInAt set")
    @ApiResponse(responseCode = "403", description = "Not an organizer or president of the event's organization",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Event or registration not found (or of another event)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "CHECK_IN_NOT_ALLOWED or ALREADY_CHECKED_IN",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ParticipantResponse checkIn(
            @PathVariable Long eventId,
            @PathVariable Long registrationId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return registrationService.checkIn(eventId, registrationId, me.id());
    }

    @GetMapping("/users/me/registrations")
    @Operation(summary = "List my registrations, newest first",
            description = "Only the caller's own registrations, CONFIRMED and CANCELLED, with the event details.")
    @ApiResponse(responseCode = "200", description = "My registrations; an empty list if there are none")
    public List<MyRegistrationResponse> listMine(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return registrationService.listMine(me.id());
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
