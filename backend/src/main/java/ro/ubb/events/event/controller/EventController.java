package ro.ubb.events.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ro.ubb.events.auth.dto.AuthenticatedUser;
import ro.ubb.events.common.dto.ApiError;
import ro.ubb.events.event.dto.EventRequest;
import ro.ubb.events.event.dto.EventResponse;
import ro.ubb.events.event.service.EventService;

/** Events (W2 creation and lifecycle, W3 discovery). */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Events", description = "Events and their lifecycle (W2, W3)")
@ApiResponse(responseCode = "401", description = "Missing, invalid or expired token",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/organizations/{orgId}/events")
    @PreAuthorize("@orgPermissions.isOrganizer(principal.id, #orgId)")
    @Operation(summary = "Create a draft event (organizer or president of the organization)",
            description = "The event starts as DRAFT. A global ADMIN has no rights here unless also an organizer.")
    @ApiResponse(responseCode = "201", description = "Event created; Location points to /api/v1/events/{id}")
    @ApiResponse(responseCode = "400", description = "VALIDATION_FAILED, e.g. endAt not after startAt, capacity <= 0, "
            + "registrationDeadline after startAt, startAt in the past",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Not an organizer or president of this organization",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<EventResponse> create(@PathVariable Long orgId,
                                                @Valid @RequestBody EventRequest request,
                                                @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        EventResponse created = eventService.create(orgId, request, me.id());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/events/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }
}
