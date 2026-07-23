// application/dto/response/ApiResponse.java
package com.serviloc.litiges.application.dto.response;

/**
 * Enveloppe standard de toutes les réponses de l'API, conforme à la section 3
 * de l'API_CONTRACT :
 *   succès : { success: true,  data: {...}, meta: {...}|null }
 *   erreur : { success: false, data: null,  error: { code, message, field } }
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        Object meta,
        ErrorDetail error
) {
    public record ErrorDetail(String code, String message, String field) {}

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, Object meta) {
        return new ApiResponse<>(true, data, meta, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, null, new ErrorDetail(code, message, null));
    }

    public static <T> ApiResponse<T> error(String code, String message, String field) {
        return new ApiResponse<>(false, null, null, new ErrorDetail(code, message, field));
    }
}
