package ro.ubb.events.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ubb.events.common.dto.ApiError;
import ro.ubb.events.organization.dto.OrganizationResponse;
import ro.ubb.events.organization.service.OrganizationService;

/**
 * Organizations (W1). Any logged-in user may read them: SecurityConfig requires a valid token
 * for every path that is not explicitly public, so no extra annotation is needed here.
 */
@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations", description = "Student organizations (W1)")
@ApiResponse(responseCode = "401", description = "Missing, invalid or expired token",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
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
}
