package com.serviloc.fichiers.application.dto;

/**
 * Enveloppe de réponse standard, conforme au contrat API ServiLoc section 3.1.
 * {@code meta} reste null sur ce service (aucun endpoint paginé).
 */
public record ApiResponse<T>(boolean success, T data, Object meta) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }
}
