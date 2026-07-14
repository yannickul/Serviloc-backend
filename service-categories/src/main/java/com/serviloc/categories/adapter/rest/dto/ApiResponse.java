package com.serviloc.categories.adapter.rest.dto;

/**
 * Enveloppe de succès standard, format API_CONTRACT.md §3.1 :
 * { "success": true, "data": {...}, "meta": null }
 */
public record ApiResponse<T>(boolean success, T data, Object meta) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> of(T data, Object meta) {
        return new ApiResponse<>(true, data, meta);
    }
}
