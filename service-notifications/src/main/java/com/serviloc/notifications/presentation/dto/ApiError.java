package com.serviloc.notifications.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Format d'erreur unique pour tous les endpoints — cf. API_CONTRACT.md §3.2/§3.3.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(String code, String message, String field) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message, null);
    }

    public static ApiError of(String code, String message, String field) {
        return new ApiError(code, message, field);
    }
}
