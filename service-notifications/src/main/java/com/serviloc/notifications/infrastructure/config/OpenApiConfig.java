package com.serviloc.notifications.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI servilocNotificationsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServiLoc — Service Notifications")
                        .description("Envoi de toutes les notifications sortantes (SMS, push FCM, email). "
                                + "Service purement consommateur d'événements RabbitMQ (exchange serviloc.events).")
                        .version("v1"));
    }
}
