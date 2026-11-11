package ro.ubb.events.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ro.ubb.events.common.dto.ApiError;
import ro.ubb.events.organization.dto.AddMemberRequest;
import ro.ubb.events.organization.dto.MemberResponse;
import ro.ubb.events.organization.dto.OrganizationResponse;
import ro.ubb.events.organization.service.MembershipService;
import ro.ubb.events.organization.service.OrganizationService;

/**
 * Organizations (W1). Any logged-in user may read the organizations themselves: SecurityConfig
 * requires a valid token for every path that is not explicitly public. The members endpoints are
 * restricted further with {@code @PreAuthorize}.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Student organizations (W1)")
@ApiResponse(responseCode = "401", description = "Missing, invalid or expired token",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
public class OrganizationController {

    private final OrganizationService organizationService;
    private final MembershipService membershipService;

    public OrganizationController(OrganizationService organizationService, MembershipService membershipService) {
        this.organizationService = organizationService;
        this.membershipService = membershipService;
    }

    @GetMapping
    @Operation(summary = "List all organizations (sorted by name)")
    @ApiResponse(responseCode = "200", description = "List of organizations")
    public List<OrganizationResponse> findAll() {
        return organizationService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one organization by id")
    @ApiResponse(responseCode = "200", description = "The organization")
    @ApiResponse(responseCode = "404", description = "Organization not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public OrganizationResponse getById(@PathVariable Long id) {
        return organizationService.getById(id);
    }

    @GetMapping("/{orgId}/members")
    @PreAuthorize("hasRole('ADMIN') or @orgPermissions.isMember(principal.id, #orgId)")
    @Operation(summary = "Members of one organization (sorted by role, then name)",
            description = "Only members of this organization and global ADMINs may see the list. "
                    + "Order: PRESIDENT, ORGANIZER, MEMBER, then full name.")
    @ApiResponse(responseCode = "200", description = "Members of the organization")
    @ApiResponse(responseCode = "403", description = "Not a member of this organization and not ADMIN",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Organization not found (ADMIN only; others get 403)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<MemberResponse> members(@PathVariable Long orgId) {
        return membershipService.findByOrganizationId(orgId);
    }

    @PostMapping("/{orgId}/members")
    @PreAuthorize("hasRole('ADMIN') or @orgPermissions.isPresident(principal.id, #orgId)")
    @Operation(summary = "Add an existing account to the organization (president or ADMIN)",
            description = "The account is found by email (ignoring case). Role MEMBER or ORGANIZER; "
                    + "presidents are only set by an admin when the organization is created.")
    @ApiResponse(responseCode = "201", description = "Member added; Location points to the new membership")
    @ApiResponse(responseCode = "400", description = "VALIDATION_FAILED: blank/invalid email, role missing or not MEMBER/ORGANIZER",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Not the president of this organization and not ADMIN",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "USER_NOT_FOUND (no account with this email), "
            + "or NOT_FOUND (organization, ADMIN only)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "ALREADY_MEMBER: the user is already in this organization",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<MemberResponse> addMember(@PathVariable Long orgId,
                                                    @Valid @RequestBody AddMemberRequest request) {
        MemberResponse member = membershipService.addMember(orgId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{membershipId}")
                .buildAndExpand(member.membershipId())
                .toUri();
        return ResponseEntity.created(location).body(member);
    }

    @DeleteMapping("/{orgId}/members/{membershipId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or @orgPermissions.isPresident(principal.id, #orgId)")
    @Operation(summary = "Remove a member from the organization (president or ADMIN)",
            description = "Tasks assigned to this membership become unassigned. The president cannot be removed.")
    @ApiResponse(responseCode = "204", description = "Member removed")
    @ApiResponse(responseCode = "403", description = "Not the president of this organization and not ADMIN",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Membership not found, or it belongs to another organization",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "CANNOT_REMOVE_PRESIDENT: the president cannot be removed",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public void removeMember(@PathVariable Long orgId, @PathVariable Long membershipId) {
        membershipService.removeMember(orgId, membershipId);
    }
}
