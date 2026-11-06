package ro.ubb.events.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.ubb.events.common.dto.HealthResponse;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Liveness check")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check that the API is up (public)")
    public HealthResponse health() {
        return new HealthResponse("UP", Instant.now());
    }
}
