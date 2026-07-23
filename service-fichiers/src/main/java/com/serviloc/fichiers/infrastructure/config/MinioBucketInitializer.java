package com.serviloc.fichiers.infrastructure.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Initialise le bucket {@code serviloc-media} au démarrage : le crée s'il
 * n'existe pas, puis applique une policy de lecture publique (GetObject) afin
 * que les URLs retournées (ex: POST /uploads/photos) soient directement
 * consultables sans authentification — voir section 3.8 de l'architecture.
 *
 * Composant séparé de {@link MinioConfig} : le {@link MinioClient} est reçu
 * par injection une fois pleinement construit par Spring, ce qui évite la
 * référence circulaire produite en appelant une méthode @Bean depuis le
 * @PostConstruct de la même classe @Configuration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBucketInitializer {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.bucket()).build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket()).build());
                log.info("Bucket MinIO '{}' créé.", properties.bucket());
            } else {
                log.info("Bucket MinIO '{}' déjà existant.", properties.bucket());
            }

            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(properties.bucket())
                    .config(publicReadPolicy(properties.bucket()))
                    .build());
            log.info("Policy de lecture publique appliquée au bucket '{}'.", properties.bucket());

        } catch (Exception e) {
            // Ne bloque pas le démarrage du service : MinIO peut être temporairement
            // indisponible en dev (ordre de démarrage Docker Compose). Le service
            // retentera l'accès à la prochaine opération d'upload.
            log.error("Impossible d'initialiser le bucket MinIO '{}' au démarrage : {}",
                    properties.bucket(), e.getMessage());
        }
    }

    /**
     * Policy IAM JSON autorisant s3:GetObject à tout le monde (public) sur le bucket.
     * Seule la lecture est publique ; write/delete restent réservés aux credentials
     * du service (access-key/secret-key applicatifs).
     */
    private String publicReadPolicy(String bucket) {
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucket);
    }
}
