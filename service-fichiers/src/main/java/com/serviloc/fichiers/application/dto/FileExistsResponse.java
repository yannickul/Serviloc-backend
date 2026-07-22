package com.serviloc.fichiers.application.dto;

/**
 * Réponse de GET /internal/files/check/{id}.
 */
public record FileExistsResponse(String id, boolean exists) {
}
