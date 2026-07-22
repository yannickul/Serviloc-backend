package com.serviloc.fichiers.application.dto;

import java.util.List;

/**
 * data de la réponse 201 pour POST /uploads/photos et POST /uploads/documents.
 */
public record UploadResponse(List<UploadItemResponse> uploads) {
}
