package com.finassist.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI finAssistOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FinAssist — Secure GenAI Personal Banking Platform API")
                        .description("Production-Style Portfolio Backend demonstrating Spring Boot 3, RAG Retrieval, Deterministic Financial Analytics, and Multi-Tier AI Guardrails.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FinAssist Engineering Team")
                                .email("engineering@finassist.co.uk"))
                        .license(new License()
                                .name("MIT Educational License")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createSecurityScheme()));
    }

    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
