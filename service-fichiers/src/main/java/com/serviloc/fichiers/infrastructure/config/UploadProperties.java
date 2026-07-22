package com.serviloc.fichiers.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Propriétés de validation d'upload, chargées depuis {@code serviloc.upload.*}.
 * Deux profils : photos (jpg/png/webp, 5 Mo) et documents (PDF/image, 10 Mo),
 * conformes à la section 10 du contrat API.
 */
@ConfigurationProperties(prefix = "serviloc.upload")
public record UploadProperties(Profile photos, Profile documents) {

    public record Profile(long maxSizeBytes, List<String> allowedMimeTypes) {
    }
}
