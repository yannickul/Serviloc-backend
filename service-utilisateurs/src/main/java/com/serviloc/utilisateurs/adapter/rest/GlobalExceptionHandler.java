package com.serviloc.utilisateurs.adapter.rest;

import com.serviloc.utilisateurs.domain.exception.EmailAlreadyExistsException;
import com.serviloc.utilisateurs.domain.exception.InvalidOtpException;
import com.serviloc.utilisateurs.domain.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── Format d'erreur unifié ───────────────────────────────────

    record ErrorDetail(String code, String message, String field) {}
    record ErrorResponse(boolean success, ErrorDetail error) {}

    // ─── Handlers ─────────────────────────────────────────────────

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage(), "email");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_OTP", ex.getMessage(), "code");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "Email ou mot de passe incorrect", null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        return error(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_ACTIVATED",
                "Compte non activé. Vérifiez votre OTP.", null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return error(HttpStatus.CONFLICT, "INVALID_STATE", ex.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        // On retourne le premier champ en erreur
        FieldError first = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().orElse(null);

        String field   = first != null ? first.getField() : null;
        String message = first != null && first.getDefaultMessage() != null
                ? first.getDefaultMessage()
                : "Validation échouée";

        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, field);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Une erreur interne est survenue", null);
    }

    // ─── Helper ───────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code,
                                                String message, String field) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(false, new ErrorDetail(code, message, field)));
    }
}