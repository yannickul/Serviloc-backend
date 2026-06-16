// application/dto/response/ApiResponse.java
package com.serviloc.mission.application.dto.response;

import java.time.Instant;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDto error;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorDto(code, message, Instant.now());
        return response;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ErrorDto getError() { return error; }

    public static class ErrorDto {
        private String code;
        private String message;
        private Instant timestamp;

        public ErrorDto(String code, String message, Instant timestamp) {
            this.code = code;
            this.message = message;
            this.timestamp = timestamp;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
        public Instant getTimestamp() { return timestamp; }
    }
}