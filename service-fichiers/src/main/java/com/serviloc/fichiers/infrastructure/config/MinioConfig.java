package com.serviloc.fichiers.infrastructure.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du client MinIO (stockage objet compatible S3).
 * L'initialisation du bucket serviloc-media (création + policy publique) est
 * effectuée par {@link MinioBucketInitializer}, un composant séparé qui reçoit
 * ce bean déjà construit par injection — l'appeler depuis un @PostConstruct de
 * cette même classe créerait une référence circulaire sur le bean MinioConfig
 * (la méthode @Bean minioClient() re-sollicite le bean en cours de création).
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    private final MinioProperties properties;

    public MinioConfig(MinioProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }
}
