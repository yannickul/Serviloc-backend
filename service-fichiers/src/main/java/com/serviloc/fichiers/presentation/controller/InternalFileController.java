package com.serviloc.fichiers.presentation.controller;

import com.serviloc.fichiers.application.dto.ApiResponse;
import com.serviloc.fichiers.application.dto.BatchUrlsRequest;
import com.serviloc.fichiers.application.dto.BatchUrlsResponse;
import com.serviloc.fichiers.application.dto.FileExistsResponse;
import com.serviloc.fichiers.application.dto.FileMetadataResponse;
import com.serviloc.fichiers.application.dto.FileUrlResponse;
import com.serviloc.fichiers.application.service.FileQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints internes, non exposés au Gateway, appelés uniquement par les autres
 * microservices via OpenFeign (header {@code X-Internal-Token} vérifié par
 * {@link com.serviloc.fichiers.infrastructure.config.InternalTokenInterceptor}).
 */
@Tag(name = "Internal Files", description = "Endpoints internes consommés par les autres microservices (Feign)")
@SecurityRequirement(name = "internalToken")
@RestController
@RequestMapping("/internal/files")
@RequiredArgsConstructor
public class InternalFileController {

    private final FileQueryService queryService;

    @Operation(summary = "Métadonnées d'un fichier", description = "Tous les services")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileMetadataResponse>> getMetadata(@PathVariable String id) {
        FileMetadataResponse metadata = queryService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(metadata));
    }

    @Operation(summary = "URL publique d'un fichier", description = "Tous les services")
    @GetMapping("/{id}/url")
    public ResponseEntity<ApiResponse<FileUrlResponse>> getUrl(@PathVariable String id) {
        String url = queryService.findUrl(id);
        return ResponseEntity.ok(ApiResponse.ok(new FileUrlResponse(id, url)));
    }

    @Operation(summary = "Supprime un fichier (physique + base)", description = "Missions, Litiges (cleanup)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        queryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "URLs de plusieurs fichiers en lot", description = "Missions, Négociations")
    @PostMapping("/batch/urls")
    public ResponseEntity<ApiResponse<BatchUrlsResponse>> batchUrls(@Valid @RequestBody BatchUrlsRequest request) {
        BatchUrlsResponse response = queryService.batchUrls(request.ids());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Vérifie l'existence d'un fichier", description = "Tous les services")
    @GetMapping("/check/{id}")
    public ResponseEntity<ApiResponse<FileExistsResponse>> checkExists(@PathVariable String id) {
        boolean exists = queryService.exists(id);
        return ResponseEntity.ok(ApiResponse.ok(new FileExistsResponse(id, exists)));
    }
}
