package org.example.carrentalsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Car Rental API")
                        .version("1.0")
                        .description("REST API for car rental service"))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }

    @Bean
    public OpenApiCustomizer tagsOrderCustomizer() {
        return openApi -> openApi.setTags(List.of(
                new Tag().name("Auth").description("Authentication and authorization operations"),
                new Tag().name("Users").description("Operations for user management"),
                new Tag().name("Car Categories").description("Operations for car category management"),
                new Tag().name("Cars").description("Operations for car management"),
                new Tag().name("Rental Locations").description("Operations for rental location management"),
                new Tag().name("Payments").description("Operations for payment management"),
                new Tag().name("Reviews").description("Operations for review management"),
                new Tag().name("Bookings").description("Operations for car bookings")
        ));
    }
}
