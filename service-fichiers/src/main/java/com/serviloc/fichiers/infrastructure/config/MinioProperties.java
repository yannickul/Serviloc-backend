package com.serviloc.fichiers.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de connexion et de configuration MinIO, chargées depuis {@code minio.*}
 * dans application.yml (elles-mêmes externalisées via variables d'environnement :
 * MINIO_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY — voir section 6.2 architecture).
 */
@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucket,
        String publicBaseUrl
) {
}
