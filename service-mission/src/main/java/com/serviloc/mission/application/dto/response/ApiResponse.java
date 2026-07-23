// application/dto/response/ApiResponse.java
package com.serviloc.mission.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDto error;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Object meta;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return error(code, message, null);
    }

    public static <T> ApiResponse<T> error(String code, String message, String field) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorDto(code, message, field);
        return response;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ErrorDto getError() { return error; }
    public Object getMeta() { return meta; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDto {
        private String code;
        private String message;
        private String field;

        public ErrorDto(String code, String message, String field) {
            this.code = code;
            this.message = message;
            this.field = field;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
        public String getField() { return field; }
    }
}