package com.serviloc.paiement.adapter.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ErrorDetail(String code, String message, String field) {}
    record ErrorResponse(boolean success, ErrorDetail error) {}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return error(HttpStatus.CONFLICT, "INVALID_STATE", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        FieldError first = ex.getBindingResult().getFieldErrors()
                .stream().findFirst().orElse(null);
        String field   = first != null ? first.getField() : null;
        String message = first != null && first.getDefaultMessage() != null
                ? first.getDefaultMessage() : "Validation échouée";
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, field);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Une erreur interne est survenue", null);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code,
                                                String message, String field) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(false, new ErrorDetail(code, message, field)));
    }
}