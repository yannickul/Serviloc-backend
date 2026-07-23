package com.serviloc.notifications.presentation.dto;

public record ApiErrorResponse(boolean success, ApiError error) {

    public static ApiErrorResponse of(ApiError error) {
        return new ApiErrorResponse(false, error);
    }
}
