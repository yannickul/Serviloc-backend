package com.serviloc.fichiers.application.dto;

import java.util.Map;

/**
 * Réponse de POST /internal/files/batch/urls — map fileId -> url.
 * Les ids demandés mais introuvables sont simplement absents de la map
 * (pas d'erreur 404 globale pour un batch partiellement valide).
 */
public record BatchUrlsResponse(Map<String, String> urls) {
}
