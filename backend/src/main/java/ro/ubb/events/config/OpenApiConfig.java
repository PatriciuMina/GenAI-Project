package ro.ubb.events.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("UBB Student Events API")
                .version("v1")
                .description("Event management for student organizations at Babeș-Bolyai University"));
    }
}
