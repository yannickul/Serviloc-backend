package com.serviloc.fichiers.application.dto;

import com.serviloc.fichiers.domain.model.FileMetadata;

import java.time.OffsetDateTime;

/**
 * Réponse de GET /internal/files/{id} — métadonnées complètes d'un fichier,
 * consommée par tous les autres microservices (Missions, Litiges, Négociations...).
 */
public record FileMetadataResponse(
        String id,
        String originalName,
        String mimeType,
        long sizeBytes,
        String url,
        String context,
        String uploadedBy,
        OffsetDateTime uploadedAt
) {
    public static FileMetadataResponse from(FileMetadata metadata) {
        return new FileMetadataResponse(
                metadata.getId().toString(),
                metadata.getOriginalName(),
                metadata.getMimeType(),
                metadata.getSize(),
                metadata.getUrl(),
                metadata.getContext(),
                metadata.getUploadedBy(),
                metadata.getUploadedAt()
        );
    }
}
