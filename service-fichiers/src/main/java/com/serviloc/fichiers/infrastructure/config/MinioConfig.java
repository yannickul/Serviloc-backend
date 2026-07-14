package com.serviloc.fichiers.infrastructure.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration du client MinIO (stockage objet compatible S3) et initialisation
 * du bucket {@code serviloc-media} au démarrage de l'application.
 *
 * Le bucket est créé automatiquement s'il n'existe pas, et une policy de lecture
 * publique (GetObject) est appliquée afin que les URLs retournées par le service
 * (ex: dans POST /uploads/photos) soient directement consultables par le frontend
 * sans authentification, comme documenté en section 3.8 de l'architecture
 * ("Génération d'URLs publiques sur CDN (ou stockage local en dev)").
 *
 * L'écriture (upload/suppression) reste réservée au service via les access/secret
 * keys — seule la LECTURE est publique.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }

    /**
     * S'exécute après la construction du bean MinioClient : vérifie l'existence
     * du bucket serviloc-media, le crée si besoin, puis applique une policy IAM
     * de lecture publique (s3:GetObject) sur tous les objets du bucket.
     */
    @PostConstruct
    public void initBucket() {
        try {
            MinioClient client = minioClient();
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.bucket()).build());

            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.bucket()).build());
                log.info("Bucket MinIO '{}' créé.", properties.bucket());
            } else {
                log.info("Bucket MinIO '{}' déjà existant.", properties.bucket());
            }

            client.setBucketPolicy(SetBucketPolicyArgs.builder()
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
