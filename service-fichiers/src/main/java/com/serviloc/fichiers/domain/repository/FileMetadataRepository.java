package com.serviloc.fichiers.domain.repository;

import com.serviloc.fichiers.domain.model.FileId;
import com.serviloc.fichiers.domain.model.FileMetadata;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance des métadonnées de fichiers (défini dans le domaine,
 * implémenté par un adapter JPA dans infrastructure/persistence).
 */
public interface FileMetadataRepository {

    FileMetadata save(FileMetadata fileMetadata);

    Optional<FileMetadata> findById(FileId id);

    List<FileMetadata> findAllByIdIn(List<FileId> ids);

    boolean existsById(FileId id);

    void deleteById(FileId id);
}
