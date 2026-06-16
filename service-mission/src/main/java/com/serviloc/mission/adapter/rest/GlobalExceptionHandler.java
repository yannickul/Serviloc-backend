// adapter/rest/GlobalExceptionHandler.java
package com.serviloc.mission.adapter.rest;

import com.serviloc.mission.application.dto.response.ApiResponse;
import com.serviloc.mission.domain.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DemandNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDemandNotFound(
            DemandNotFoundException ex) {
        log.warn("Demande introuvable : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("DEMAND_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MissionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissionNotFound(
            MissionNotFoundException ex) {
        log.warn("Mission introuvable : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("MISSION_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedMissionAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedMissionAccessException ex) {
        log.warn("Accès refusé : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", ex.getMessage()));
    }

    @ExceptionHandler(DoubleValidationAlreadyDoneException.class)
    public ResponseEntity<ApiResponse<Void>> handleDoubleValidation(
            DoubleValidationAlreadyDoneException ex) {
        log.warn("Double validation : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("ALREADY_VALIDATED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation échouée : {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex) {
        log.warn("Argument invalide : {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_ARGUMENT", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Erreur inattendue : {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR",
                        "Une erreur interne est survenue"));
    }
}