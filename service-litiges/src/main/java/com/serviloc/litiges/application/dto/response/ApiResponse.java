// application/dto/response/ApiResponse.java
package com.serviloc.litiges.application.dto.response;

public record ApiResponse<T>(
        boolean success,
        T data,
        String errorCode,
        String errorMessage
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, code, message);
    }
}