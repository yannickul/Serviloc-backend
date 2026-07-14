package com.serviloc.fichiers.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI serviceFichiersOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServiLoc — Service Fichiers (Media)")
                        .description("Upload de photos et documents, stockage MinIO, "
                                + "endpoints internes de consultation pour les autres microservices.")
                        .version("v1")
                        .contact(new Contact().name("ServiLoc Backend Team")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .addSecurityItem(new SecurityRequirement().addList("internalToken"))
                .schemaRequirement("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token JWT utilisateur, injecté par le Gateway (X-User-Id / X-User-Role)."))
                .schemaRequirement("internalToken", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Internal-Token")
                        .description("Token machine partagé, utilisé sur les endpoints /internal/** "
                                + "appelés en interne (Feign) par les autres microservices."));
    }
}
