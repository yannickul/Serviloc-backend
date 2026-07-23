package com.serviloc.fichiers.application.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Requête de POST /internal/files/batch/urls.
 */
public record BatchUrlsRequest(@NotEmpty List<String> ids) {
}
