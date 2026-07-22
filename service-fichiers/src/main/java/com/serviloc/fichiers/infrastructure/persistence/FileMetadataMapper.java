package com.serviloc.fichiers.infrastructure.persistence;

import com.serviloc.fichiers.domain.model.FileId;
import com.serviloc.fichiers.domain.model.FileMetadata;
import org.springframework.stereotype.Component;

@Component
public class FileMetadataMapper {

    public FileMetadataJpaEntity toJpaEntity(FileMetadata domain) {
        return new FileMetadataJpaEntity(
                domain.getId().value(),
                domain.getOriginalName(),
                domain.getMimeType(),
                domain.getSize(),
                domain.getUrl(),
                domain.getContext(),
                domain.getUploadedBy(),
                domain.getUploadedAt()
        );
    }

    public FileMetadata toDomain(FileMetadataJpaEntity entity) {
        return FileMetadata.reconstruct(
                FileId.of(entity.getId()),
                entity.getOriginalName(),
                entity.getMimeType(),
                entity.getSize(),
                entity.getUrl(),
                entity.getContext(),
                entity.getUploadedBy(),
                entity.getUploadedAt()
        );
    }
}
