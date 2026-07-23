package com.serviloc.fichiers;

import com.serviloc.fichiers.infrastructure.config.UploadProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Service Fichiers (Media) — ServiLoc.
 *
 * Bounded Context : stockage et accès aux fichiers binaires (photos, documents).
 * Aucun événement RabbitMQ émis ou consommé — service purement synchrone.
 *
 * Port : 8088 | DB : db_media (PostgreSQL, port hôte 5448) | Stockage : MinIO (bucket serviloc-media)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableConfigurationProperties(UploadProperties.class)
public class ServiceFichiersApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceFichiersApplication.class, args);
    }
}
