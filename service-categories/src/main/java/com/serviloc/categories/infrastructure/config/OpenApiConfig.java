package com.serviloc.categories.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI servilocCategoriesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServiLoc — Service Catégories")
                        .version("1.0.0")
                        .description("Référentiel des catégories de services (voir API_CONTRACT.md §6, §8 "
                                + "et ARCHITECTURE_MICROSERVICES_SERVILOC.md §3.7)"))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token injecté par le Gateway via X-User-Id / X-User-Role"))
                        .addSecuritySchemes("internal-token", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Internal-Token")
                                .description("Token machine pour les appels Feign inter-services")));
    }
}
