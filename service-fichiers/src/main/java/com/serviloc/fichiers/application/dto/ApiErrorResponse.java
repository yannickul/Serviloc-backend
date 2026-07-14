package com.serviloc.fichiers.application.dto;

/**
 * Format unique d'erreur pour TOUS les endpoints, conforme au contrat API
 * ServiLoc section 3.2.
 */
public record ApiErrorResponse(boolean success, ApiError error) {

    public record ApiError(String code, String message, String field) {
    }

    public static ApiErrorResponse of(String code, String message, String field) {
        return new ApiErrorResponse(false, new ApiError(code, message, field));
    }

    public static ApiErrorResponse of(String code, String message) {
        return of(code, message, null);
    }
}
