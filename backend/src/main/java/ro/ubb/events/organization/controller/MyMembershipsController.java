package ro.ubb.events.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ubb.events.auth.dto.AuthenticatedUser;
import ro.ubb.events.common.dto.ApiError;
import ro.ubb.events.organization.dto.MembershipResponse;
import ro.ubb.events.organization.service.MembershipService;

/**
 * The logged-in user's organization memberships (W1). Lives in the organization feature (not in
 * {@code UserController}) so the user package does not depend on the organization package.
 * The user id always comes from the token, never from the URL.
 */
@RestController
@RequestMapping("/api/v1/users/me/memberships")
@Tag(name = "Organizations", description = "Student organizations (W1)")
public class MyMembershipsController {

    private final MembershipService membershipService;

    public MyMembershipsController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    @Operation(summary = "My organizations and my role in each (sorted by organization name)")
    @ApiResponse(responseCode = "200", description = "My memberships (empty list if I am in no organization)")
    @ApiResponse(responseCode = "401", description = "Missing, invalid or expired token",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<MembershipResponse> myMemberships(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return membershipService.findByUserId(me.id());
    }
}
