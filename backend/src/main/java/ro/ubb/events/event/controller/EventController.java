package ro.ubb.events.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ro.ubb.events.auth.dto.AuthenticatedUser;
import ro.ubb.events.common.dto.ApiError;
import ro.ubb.events.common.dto.PageResponse;
import ro.ubb.events.event.entity.EventCategory;
import ro.ubb.events.event.dto.EventRequest;
import ro.ubb.events.event.dto.EventResponse;
import ro.ubb.events.event.dto.UpdateEventRequest;
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

    @GetMapping("/events")
    @Operation(summary = "Search visible upcoming events", description = "Returns visible events in start-time order, with optional filters.")
    @ApiResponse(responseCode = "200", description = "Paged event search results")
    @ApiResponse(responseCode = "400", description = "Invalid category, date or paging parameter",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public PageResponse<EventResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return eventService.search(q, category, organizationId, from, to, page, size);
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

    @GetMapping("/events/{id}")
    @Operation(summary = "Get an event by id", description = "DRAFT events are visible only to organizers of their organization.")
    @ApiResponse(responseCode = "200", description = "Event details, including confirmed registrations and free places")
    @ApiResponse(responseCode = "404", description = "Event not found or a hidden draft",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public EventResponse getById(@PathVariable Long id,
                                 @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return eventService.getById(id, me.id());
    }

    @PutMapping("/events/{id}")
    @Operation(summary = "Edit a draft or published event", description = "Requires the version from the latest EventResponse.")
    @ApiResponse(responseCode = "200", description = "Event updated")
    @ApiResponse(responseCode = "400", description = "Invalid event fields or capacity below confirmed registrations",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Not an organizer of the event's organization",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "EVENT_NOT_EDITABLE or CONCURRENT_UPDATE",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest request,
                                @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return eventService.update(id, request, me.id());
    }
}
