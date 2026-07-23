package com.serviloc.fichiers.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entité JPA persistée dans db_media, table file_metadata.
 * Séparée du modèle domaine {@link com.serviloc.fichiers.domain.model.FileMetadata}
 * (mappée par {@link FileMetadataMapper}) — jamais exposée hors de la couche infrastructure.
 */
@Entity
@Table(
        name = "file_metadata",
        indexes = {
                @Index(name = "idx_file_metadata_uploaded_by", columnList = "uploaded_by"),
                @Index(name = "idx_file_metadata_context", columnList = "context")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataJpaEntity {

    @Id
    private UUID id;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long size;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "context", length = 100)
    private String context;

    @Column(name = "uploaded_by", nullable = false, length = 100)
    private String uploadedBy;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;
}
