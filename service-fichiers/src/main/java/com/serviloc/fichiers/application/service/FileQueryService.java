package com.serviloc.fichiers.application.service;

import com.serviloc.fichiers.application.dto.BatchUrlsResponse;
import com.serviloc.fichiers.application.dto.FileMetadataResponse;
import com.serviloc.fichiers.domain.exception.FileNotFoundException;
import com.serviloc.fichiers.domain.model.FileId;
import com.serviloc.fichiers.domain.model.FileMetadata;
import com.serviloc.fichiers.domain.repository.FileMetadataRepository;
import com.serviloc.fichiers.domain.repository.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use cases de consultation/suppression exposés aux endpoints {@code /internal/files/**},
 * appelés en interne (Feign, header X-Internal-Token) par les autres microservices
 * (Missions, Litiges, Négociations...).
 *
 * Les lectures (findById/findUrl) sont mises en cache Redis (TTL 10 min, voir
 * application.yml spring.cache.redis.time-to-live) car les métadonnées d'un
 * fichier ne changent jamais après upload — seul un delete doit invalider le cache.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "file-metadata")
public class FileQueryService {

    private final FileMetadataRepository metadataRepository;
    private final FileStoragePort storagePort;

    @Cacheable(key = "#fileId")
    @Transactional(readOnly = true)
    public FileMetadataResponse findById(String fileId) {
        FileMetadata metadata = metadataRepository.findById(FileId.of(fileId))
                .orElseThrow(() -> new FileNotFoundException(fileId));
        return FileMetadataResponse.from(metadata);
    }

    @Cacheable(key = "#fileId + '-url'")
    @Transactional(readOnly = true)
    public String findUrl(String fileId) {
        FileMetadata metadata = metadataRepository.findById(FileId.of(fileId))
                .orElseThrow(() -> new FileNotFoundException(fileId));
        return metadata.getUrl();
    }

    @Transactional(readOnly = true)
    public boolean exists(String fileId) {
        return metadataRepository.existsById(FileId.of(fileId));
    }

    @Transactional(readOnly = true)
    public BatchUrlsResponse batchUrls(List<String> fileIds) {
        List<FileId> ids = fileIds.stream().map(FileId::of).toList();
        List<FileMetadata> found = metadataRepository.findAllByIdIn(ids);
        Map<String, String> urls = new HashMap<>();
        found.forEach(metadata -> urls.put(metadata.getId().toString(), metadata.getUrl()));
        return new BatchUrlsResponse(urls);
    }

    /**
     * Supprime le fichier : d'abord l'objet binaire MinIO, puis l'entrée base.
     * L'ordre importe peu fonctionnellement mais évite une entrée orpheline en
     * base si la suppression MinIO échoue (on préfère un objet MinIO orphelin,
     * détectable et rejouable, plutôt qu'une entrée base pointant vers rien).
     */
    @Caching(evict = {
            @CacheEvict(key = "#fileId"),
            @CacheEvict(key = "#fileId + '-url'")
    })
    @Transactional
    public void delete(String fileId) {
        FileId id = FileId.of(fileId);
        FileMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        String objectKey = extractObjectKey(metadata.getUrl());
        storagePort.delete(objectKey);
        metadataRepository.deleteById(id);

        log.info("Fichier supprimé : id={} objectKey={}", fileId, objectKey);
    }

    /** Reconstruit la clé objet MinIO ("photos/xxx.jpg") à partir de l'URL publique stockée. */
    private String extractObjectKey(String url) {
        String path = URI.create(url).getPath();
        int bucketSeparator = path.indexOf('/', 1);
        return path.substring(bucketSeparator + 1);
    }
}
