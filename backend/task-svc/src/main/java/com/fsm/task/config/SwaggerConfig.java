package com.fsm.task.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for Task Management API documentation
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI taskServiceAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Management Service API")
                        .description("REST API for managing field service tasks")
                        .version("1.0.0"));
    }
}
