package com.serviloc.fichiers.application.service;

import com.serviloc.fichiers.application.dto.UploadItemResponse;
import com.serviloc.fichiers.application.dto.UploadResponse;
import com.serviloc.fichiers.domain.exception.FileTooLargeException;
import com.serviloc.fichiers.domain.exception.InvalidFileException;
import com.serviloc.fichiers.domain.exception.UnsupportedFileTypeException;
import com.serviloc.fichiers.domain.model.FileMetadata;
import com.serviloc.fichiers.domain.repository.FileMetadataRepository;
import com.serviloc.fichiers.domain.repository.FileStoragePort;
import com.serviloc.fichiers.infrastructure.config.UploadProperties;
import com.serviloc.fichiers.infrastructure.storage.MimeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Use case : upload de photos et de documents.
 *
 * Pipeline de validation par fichier (ordre important) :
 *   1. taille <= limite du contexte (413 FILE_TOO_LARGE)
 *   2. détection du type réel par analyse du contenu binaire, pas l'extension (Tika)
 *   3. si type détecté dangereux/exécutable → 415 UNSUPPORTED_MEDIA_TYPE
 *   4. si type détecté hors de la liste autorisée pour le contexte → 400 VALIDATION_ERROR
 *   5. upload vers MinIO, persistance des métadonnées
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    /** Types dont la présence indique un exécutable/binaire dangereux, quel que soit le contexte. */
    private static final Set<String> DANGEROUS_MIME_TYPES = Set.of(
            "application/x-msdownload",
            "application/x-dosexec",
            "application/vnd.microsoft.portable-executable",
            "application/x-executable",
            "application/x-sh",
            "application/x-bat",
            "application/java-archive",
            "application/x-msdos-program"
    );

    private final FileStoragePort storagePort;
    private final FileMetadataRepository metadataRepository;
    private final MimeDetector mimeDetector;
    private final UploadProperties uploadProperties;

    @Transactional
    public UploadResponse uploadPhotos(List<MultipartFile> photos, String context, String uploadedBy) {
        if (photos == null || photos.isEmpty()) {
            throw new InvalidFileException("Aucune photo fournie");
        }
        List<UploadItemResponse> uploaded = photos.stream()
                .map(photo -> uploadOne(photo, context, uploadedBy, uploadProperties.photos(), "photos"))
                .toList();
        return new UploadResponse(uploaded);
    }

    @Transactional
    public UploadResponse uploadDocument(MultipartFile document, String type, String uploadedBy) {
        if (document == null || document.isEmpty()) {
            throw new InvalidFileException("Aucun document fourni");
        }
        UploadItemResponse item = uploadOne(document, type, uploadedBy, uploadProperties.documents(), "documents");
        return new UploadResponse(List.of(item));
    }

    private UploadItemResponse uploadOne(MultipartFile file, String context, String uploadedBy,
                                          UploadProperties.Profile profile, String folder) {
        // 1. Taille
        if (file.getSize() > profile.maxSizeBytes()) {
            throw new FileTooLargeException(
                    "Fichier '%s' trop volumineux (%d octets, max %d octets)"
                            .formatted(file.getOriginalFilename(), file.getSize(), profile.maxSizeBytes()));
        }

        // 2. Détection du type réel du contenu (magic bytes), pas seulement l'extension
        String detectedMimeType = detectRealMimeType(file);

        // 3. Types dangereux → 415, quel que soit le contexte
        if (DANGEROUS_MIME_TYPES.contains(detectedMimeType)) {
            throw new UnsupportedFileTypeException(
                    "Type de fichier non supporté pour '%s' : %s"
                            .formatted(file.getOriginalFilename(), detectedMimeType));
        }

        // 4. Type hors liste autorisée pour ce contexte → 400
        if (!profile.allowedMimeTypes().contains(detectedMimeType)) {
            throw new InvalidFileException(
                    "Format invalide pour '%s' : %s (formats acceptés : %s)"
                            .formatted(file.getOriginalFilename(), detectedMimeType, profile.allowedMimeTypes()));
        }

        // 5. Upload MinIO + persistance métadonnées
        String objectKey = folder + "/" + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
        String url;
        try (InputStream stream = file.getInputStream()) {
            url = storagePort.store(objectKey, stream, file.getSize(), detectedMimeType);
        } catch (IOException e) {
            throw new InvalidFileException("Impossible de lire le fichier '%s'".formatted(file.getOriginalFilename()));
        }

        FileMetadata metadata = FileMetadata.create(
                file.getOriginalFilename(), detectedMimeType, file.getSize(), url, context, uploadedBy);
        metadataRepository.save(metadata);

        log.info("Fichier uploadé : id={} name={} size={} mimeType={}",
                metadata.getId(), metadata.getOriginalName(), metadata.getSize(), detectedMimeType);

        return new UploadItemResponse(metadata.getId().toString(), url, metadata.getOriginalName(), metadata.getSize());
    }

    private String detectRealMimeType(MultipartFile file) {
        try (BufferedInputStream buffered = new BufferedInputStream(file.getInputStream())) {
            return mimeDetector.detect(buffered, file.getOriginalFilename());
        } catch (IOException e) {
            throw new InvalidFileException("Impossible d'analyser le type du fichier '%s'"
                    .formatted(file.getOriginalFilename()));
        }
    }

    private String sanitize(String fileName) {
        if (fileName == null) {
            return "fichier";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
