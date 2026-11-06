package ro.ubb.events.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI. Declares a "bearerAuth" scheme and requires it globally, which adds
 * the "Authorize" button: paste the {@code accessToken} from {@code POST /api/v1/auth/login}
 * (without the "Bearer " prefix). Public endpoints opt out with {@code @SecurityRequirements()}.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI eventsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("UBB Student Events API")
                        .version("v1")
                        .description("Event management for student organizations at Babeș-Bolyai University. "
                                + "Log in with POST /api/v1/auth/login, then press \"Authorize\" and paste the accessToken."))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
