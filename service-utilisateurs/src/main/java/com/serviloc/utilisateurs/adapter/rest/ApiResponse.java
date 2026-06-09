package com.serviloc.utilisateurs.adapter.rest;

/**
 * Wrapper de réponse unifié — contrat API v2.0.
 * Format : { "success": true, "data": {...}, "meta": {...} }
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        Object meta
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> ok(T data, Object meta) {
        return new ApiResponse<>(true, data, meta);
    }
}