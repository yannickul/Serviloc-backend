package com.serviloc.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Service Notifications — ServiLoc.
 *
 * Bounded context : envoi de toutes les notifications sortantes (SMS, push FCM, email).
 * Ce service ne publie aucun événement métier : il est purement consommateur
 * des événements RabbitMQ émis par les autres microservices (exchange {@code serviloc.events}).
 *
 * Port : 8086 — DB : db_notifications (port hôte 5446 en local).
 */
@SpringBootApplication
@EnableDiscoveryClient
@ConfigurationPropertiesScan
public class ServiLocNotificationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiLocNotificationsApplication.class, args);
    }
}
