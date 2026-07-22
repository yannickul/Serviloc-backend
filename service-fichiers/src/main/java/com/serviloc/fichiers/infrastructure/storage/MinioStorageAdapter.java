package com.serviloc.fichiers.infrastructure.storage;

import com.serviloc.fichiers.domain.repository.FileStoragePort;
import com.serviloc.fichiers.infrastructure.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Adapter du port {@link FileStoragePort} basé sur le SDK MinIO.
 * Construit l'URL publique de lecture à partir de {@code minio.public-base-url}
 * (bucket configuré en lecture publique par {@link com.serviloc.fichiers.infrastructure.config.MinioConfig}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageAdapter implements FileStoragePort {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public String store(String objectKey, InputStream data, long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectKey)
                    .stream(data, size, -1)
                    .contentType(contentType)
                    .build());
            String url = properties.publicBaseUrl() + "/" + objectKey;
            log.debug("Fichier stocké dans MinIO : {}", url);
            return url;
        } catch (Exception e) {
            log.error("Échec de l'upload MinIO pour l'objet '{}' : {}", objectKey, e.getMessage());
            throw new StorageException("Échec du stockage du fichier : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.bucket())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            log.error("Échec de la suppression MinIO pour l'objet '{}' : {}", objectKey, e.getMessage());
            throw new StorageException("Échec de la suppression du fichier : " + e.getMessage(), e);
        }
    }
}
