package com.serviloc.fichiers.presentation.controller;

import com.serviloc.fichiers.application.dto.ApiResponse;
import com.serviloc.fichiers.application.dto.UploadResponse;
import com.serviloc.fichiers.application.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Endpoints publics d'upload — accessibles à tous les rôles authentifiés
 * (contrat API section 10). Le Gateway injecte {@code X-User-Id} depuis le JWT.
 */
@Tag(name = "Uploads", description = "Upload de photos et documents (accessible à tous les rôles authentifiés)")
@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final FileUploadService uploadService;

    @Operation(summary = "Upload de photos",
            description = "jpg/png/webp, max 5 Mo par fichier. Validation MIME sur le contenu réel.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Photos uploadées"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "VALIDATION_ERROR — format invalide",
                    content = @Content(schema = @Schema(implementation = com.serviloc.fichiers.application.dto.ApiErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413",
                    description = "FILE_TOO_LARGE — taille > 5 Mo"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415",
                    description = "Type de fichier non supporté (ex: exécutable)")
    })
    @PostMapping(value = "/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadResponse>> uploadPhotos(
            @Parameter(description = "Fichiers photos (jpg/png/webp)") @RequestParam("photos") List<MultipartFile> photos,
            @Parameter(description = "Contexte métier (ex: demande, litige, chat)") @RequestParam(value = "context", required = false) String context,
            @RequestHeader("X-User-Id") String uploadedBy) {
        UploadResponse response = uploadService.uploadPhotos(photos, context, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "Upload d'un document",
            description = "PDF/image, max 10 Mo. Validation MIME sur le contenu réel.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document uploadé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "VALIDATION_ERROR — format invalide"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "FILE_TOO_LARGE — taille > 10 Mo"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "Type de fichier non supporté (ex: exécutable)")
    })
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadResponse>> uploadDocument(
            @Parameter(description = "Document (PDF ou image)") @RequestParam("document") MultipartFile document,
            @Parameter(description = "Type de document (ex: carte_pro, cni, casier, assurance)") @RequestParam(value = "type", required = false) String type,
            @RequestHeader("X-User-Id") String uploadedBy) {
        UploadResponse response = uploadService.uploadDocument(document, type, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
