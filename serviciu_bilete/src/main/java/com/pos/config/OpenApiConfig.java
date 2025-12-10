package com.pos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventFlow API")
                        .description("Event Ticketing Management System - RESTful API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Robert")
                                .email("robert@university.edu")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server")
                ));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("eventflow-api")
                .packagesToScan("com.pos.controller")
                .pathsToMatch("/api/**")
                .build();
    }
}