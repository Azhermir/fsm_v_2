package com.fsm.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Management Service API")
                        .description("RESTful API for managing users, technicians, and authentication in Field Service Management system")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("FSM Team")
                                .email("support@fsm.com")));
    }
}
