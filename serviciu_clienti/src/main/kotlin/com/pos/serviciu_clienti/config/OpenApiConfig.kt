package com.pos.serviciu_clienti.config


import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun clientServiceOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("EventFlow - Client Service API")
                    .description("Serviciu RESTful pentru gestiunea clienților si biletelor achiziționate. Parte din platforma EventFlow pentru gestiunea evenimentelor.")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Robert")
                            .email("robert@university.edu")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8081")
                        .description("Development Server - Client Service")
                )
            )
    }
}