package com.serviloc.files.controller;

import com.serviloc.files.entity.StoredFile;
import com.serviloc.files.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "Gestion des fichiers (upload, metadata, delete)")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @Operation(summary = "Uploader un fichier vers MinIO")
    public ResponseEntity<StoredFile> upload(@RequestParam("file") MultipartFile file) throws Exception {
        StoredFile stored = fileStorageService.upload(file);
        return ResponseEntity.ok(stored);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer les métadonnées d'un fichier")
    public ResponseEntity<StoredFile> get(@PathVariable Long id) {
        return ResponseEntity.ok(fileStorageService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un fichier (métadonnées + objet)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fileStorageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Exemple endpoint interne
    @GetMapping("/internal/health")
    @Operation(summary = "Endpoint interne de healthcheck")
    public ResponseEntity<String> internalHealth() {
        return ResponseEntity.ok("OK");
    }
}
