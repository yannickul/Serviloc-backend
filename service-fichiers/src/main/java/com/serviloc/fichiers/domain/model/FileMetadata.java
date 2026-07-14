package com.serviloc.fichiers.domain.model;

import java.time.OffsetDateTime;

/**
 * Entité domaine représentant les métadonnées d'un fichier stocké (photo ou document).
 *
 * Champs conformes au contrat métier :
 * { id, originalName, mimeType, size, url, context, uploadedBy, uploadedAt }
 *
 * Framework-free : aucune annotation Spring/JPA ici (voir infrastructure/persistence
 * pour l'entité JPA correspondante et son mapper).
 */
public class FileMetadata {

    private final FileId id;
    private final String originalName;
    private final String mimeType;
    private final long size;
    private final String url;
    private final String context;
    private final String uploadedBy;
    private final OffsetDateTime uploadedAt;

    private FileMetadata(FileId id, String originalName, String mimeType, long size,
                          String url, String context, String uploadedBy, OffsetDateTime uploadedAt) {
        this.id = id;
        this.originalName = originalName;
        this.mimeType = mimeType;
        this.size = size;
        this.url = url;
        this.context = context;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    /**
     * Factory method appelée après un upload réussi vers MinIO : enforce les
     * invariants métier (nom non vide, taille positive, url non vide, etc.)
     */
    public static FileMetadata create(String originalName, String mimeType, long size,
                                       String url, String context, String uploadedBy) {
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("originalName ne peut pas être vide");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("mimeType ne peut pas être vide");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size doit être positif");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url ne peut pas être vide");
        }
        if (uploadedBy == null || uploadedBy.isBlank()) {
            throw new IllegalArgumentException("uploadedBy ne peut pas être vide");
        }
        return new FileMetadata(FileId.generate(), originalName, mimeType, size,
                url, context, uploadedBy, OffsetDateTime.now());
    }

    /**
     * Reconstruction depuis la persistance — utilisée uniquement par le mapper
     * infrastructure, jamais depuis la couche application.
     */
    public static FileMetadata reconstruct(FileId id, String originalName, String mimeType, long size,
                                            String url, String context, String uploadedBy,
                                            OffsetDateTime uploadedAt) {
        return new FileMetadata(id, originalName, mimeType, size, url, context, uploadedBy, uploadedAt);
    }

    public FileId getId() {
        return id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

    public String getContext() {
        return context;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }
}
