package com.serviloc.files.service;

import com.serviloc.files.entity.StoredFile;
import com.serviloc.files.repository.FileRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;
    private final FileRepository fileRepository;

    @Value("${minio.bucket}")
    private String bucket;

    public StoredFile upload(MultipartFile file) throws Exception {
        String objectName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        String url = bucket + "/" + objectName; // URL logique, la Gateway fera le proxy

        StoredFile stored = StoredFile.builder()
                .filename(objectName)
                .contentType(file.getContentType())
                .url(url)
                .sizeBytes(file.getSize())
                .build();

        return fileRepository.save(stored);
    }

    public StoredFile getById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found: " + id));
    }

    public void delete(Long id) {
        fileRepository.deleteById(id);
        // Optionnel : supprimer aussi dans MinIO (removeObject)
    }
}
