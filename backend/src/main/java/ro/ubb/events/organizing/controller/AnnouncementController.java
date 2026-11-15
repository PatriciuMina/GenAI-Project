package ro.ubb.events.organizing.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ro.ubb.events.auth.dto.AuthenticatedUser;
import ro.ubb.events.common.dto.ApiError;
import ro.ubb.events.organizing.dto.AnnouncementRequest;
import ro.ubb.events.organizing.dto.AnnouncementResponse;
import ro.ubb.events.organizing.service.AnnouncementService;

/** Event announcements (W5). */
@RestController
@RequestMapping("/api/v1/events/{eventId}/announcements")
@Tag(name = "Announcements", description = "Announcements of an event (W5)")
@ApiResponse(responseCode = "401", description = "Missing, invalid or expired token",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    @Operation(summary = "List the announcements of an event, newest first",
            description = "Visible to members of the event's organization and to users with a CONFIRMED or "
                    + "CANCELLED registration for the event.")
    @ApiResponse(responseCode = "200", description = "Announcements, newest first")
    @ApiResponse(responseCode = "403", description = "Not a member of the organization and not registered",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<AnnouncementResponse> list(
            @PathVariable Long eventId,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return announcementService.list(eventId, me.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Post an announcement (organizer or president of the event's organization)",
            description = "Not allowed on DRAFT or COMPLETED events. The author is the caller.")
    @ApiResponse(responseCode = "201", description = "Announcement created")
    @ApiResponse(responseCode = "400", description = "VALIDATION_FAILED: title 1-200 characters, body not blank",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Not an organizer or president of the event's organization",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Event not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "ANNOUNCEMENT_NOT_ALLOWED (event is DRAFT or COMPLETED)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public AnnouncementResponse create(
            @PathVariable Long eventId,
            @Valid @RequestBody AnnouncementRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return announcementService.create(eventId, request, me.id());
    }
}
