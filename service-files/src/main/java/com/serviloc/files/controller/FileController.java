package com.serviloc.files.controller;

import com.serviloc.files.entity.StoredFile;
import com.serviloc.files.repository.FileRepository;
import com.serviloc.files.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;

@RestController
@RequestMapping("") // ⚠️ plus de /api/files ici
@Tag(name = "Files", description = "Gestion des fichiers (upload, download, metadata, delete, list)")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;
    private final FileRepository repository;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucketName;

    // --- Upload ---
    @PostMapping("/upload")
    @Operation(summary = "Uploader un fichier vers MinIO")
    public ResponseEntity<StoredFile> upload(@RequestParam("file") MultipartFile file) throws Exception {
        StoredFile stored = fileStorageService.upload(file);
        return ResponseEntity.ok(stored);
    }

    // --- Metadata ---
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer les métadonnées d'un fichier")
    public ResponseEntity<StoredFile> get(@PathVariable Long id) {
        return ResponseEntity.ok(fileStorageService.getById(id));
    }

    // --- Download ---
    @GetMapping("/{id}/download")
    @Operation(summary = "Télécharger un fichier depuis MinIO")
    public ResponseEntity<?> downloadFile(@PathVariable Long id) {
        Optional<StoredFile> fileOpt = repository.findById(id);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        StoredFile file = fileOpt.get();
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();

            InputStream is = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(file.getFilename())
                            .build()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .body(new InputStreamResource(is));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error downloading file: " + e.getMessage());
        }
    }

    // --- Delete ---
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un fichier (métadonnées + objet)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            fileStorageService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- List avec pagination et filtres ---
    @GetMapping("/list")
    @Operation(summary = "Lister les fichiers avec pagination et filtres")
    public ResponseEntity<Page<StoredFile>> listFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) String contentType) {

        PageRequest pageable = PageRequest.of(page, size);

        if (filename != null && !filename.isEmpty()) {
            return ResponseEntity.ok(repository.findByFilenameContainingIgnoreCase(filename, pageable));
        } else if (contentType != null && !contentType.isEmpty()) {
            return ResponseEntity.ok(repository.findByContentType(contentType, pageable));
        } else {
            return ResponseEntity.ok(repository.findAll(pageable));
        }
    }

    // --- Healthcheck interne ---
    @GetMapping("/internal/health")
    @Operation(summary = "Endpoint interne de healthcheck")
    public ResponseEntity<String> internalHealth() {
        return ResponseEntity.ok("OK");
    }
}
