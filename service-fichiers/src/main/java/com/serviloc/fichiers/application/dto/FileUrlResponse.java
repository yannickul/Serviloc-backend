package com.serviloc.fichiers.application.dto;

/**
 * Réponse de GET /internal/files/{id}/url.
 */
public record FileUrlResponse(String id, String url) {
}
