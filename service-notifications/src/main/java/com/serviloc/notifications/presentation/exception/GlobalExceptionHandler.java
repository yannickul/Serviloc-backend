package com.serviloc.notifications.presentation.exception;

import com.serviloc.notifications.domain.exception.DomainException;
import com.serviloc.notifications.presentation.dto.ApiError;
import com.serviloc.notifications.presentation.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestionnaire d'erreurs global — toutes les réponses d'erreur suivent le format unique
 * {@code {success:false, error:{code, message, field}}} défini en API_CONTRACT.md §3.2.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst();
        String field = fieldError.map(org.springframework.validation.FieldError::getField).orElse(null);
        String message = fieldError.map(org.springframework.validation.FieldError::getDefaultMessage)
                .orElse("Données de formulaire invalides");

        ApiError error = ApiError.of("VALIDATION_ERROR", message, field);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse.of(error));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(DomainException ex) {
        ApiError error = ApiError.of("VALIDATION_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse.of(error));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ApiError error = ApiError.of("VALIDATION_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse.of(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error("[Notifications] Erreur serveur inattendue", ex);
        ApiError error = ApiError.of("SERVER_ERROR", "Erreur serveur — contacter le backend");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiErrorResponse.of(error));
    }
}
