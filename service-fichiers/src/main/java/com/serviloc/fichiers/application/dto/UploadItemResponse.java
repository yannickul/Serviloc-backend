package com.serviloc.fichiers.application.dto;

/**
 * Un élément de {@code data.uploads[]}, conforme au contrat :
 * { "id", "url", "name", "sizeBytes" }
 */
public record UploadItemResponse(String id, String url, String name, long sizeBytes) {
}
