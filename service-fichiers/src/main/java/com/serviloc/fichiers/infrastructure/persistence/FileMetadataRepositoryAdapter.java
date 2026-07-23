package com.serviloc.fichiers.infrastructure.persistence;

import com.serviloc.fichiers.domain.model.FileId;
import com.serviloc.fichiers.domain.model.FileMetadata;
import com.serviloc.fichiers.domain.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter JPA du port {@link FileMetadataRepository}. Traduit entre le modèle
 * domaine (framework-free) et l'entité JPA persistée via {@link FileMetadataMapper}.
 */
@Component
@RequiredArgsConstructor
public class FileMetadataRepositoryAdapter implements FileMetadataRepository {

    private final FileMetadataJpaRepository jpaRepository;
    private final FileMetadataMapper mapper;

    @Override
    public FileMetadata save(FileMetadata fileMetadata) {
        FileMetadataJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(fileMetadata));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<FileMetadata> findById(FileId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<FileMetadata> findAllByIdIn(List<FileId> ids) {
        List<java.util.UUID> uuids = ids.stream().map(FileId::value).toList();
        return jpaRepository.findAllByIdIn(uuids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsById(FileId id) {
        return jpaRepository.existsById(id.value());
    }

    @Override
    public void deleteById(FileId id) {
        jpaRepository.deleteById(id.value());
    }
}
