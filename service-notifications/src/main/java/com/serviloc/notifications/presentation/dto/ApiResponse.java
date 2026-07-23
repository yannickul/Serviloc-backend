package com.serviloc.notifications.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Enveloppe de réponse standard ServiLoc — cf. API_CONTRACT.md §3.1.
 * {@code meta} n'est présent que sur les endpoints paginés (laissé à null sinon, exclu du JSON).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, PageMeta meta) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> ok(T data, PageMeta meta) {
        return new ApiResponse<>(true, data, meta);
    }
}
