package com.serviloc.categories.adapter.rest.dto;

/**
 * Format d'erreur unique, API_CONTRACT.md §3.2 :
 * { "success": false, "error": { "code", "message", "field" } }
 */
public record ApiErrorResponse(boolean success, ErrorBody error) {

    public record ErrorBody(String code, String message, String field) {
    }

    public static ApiErrorResponse of(String code, String message, String field) {
        return new ApiErrorResponse(false, new ErrorBody(code, message, field));
    }
}
