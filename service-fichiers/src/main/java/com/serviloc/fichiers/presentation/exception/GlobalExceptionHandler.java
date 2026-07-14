package com.serviloc.fichiers.presentation.exception;

import com.serviloc.fichiers.application.dto.ApiErrorResponse;
import com.serviloc.fichiers.domain.exception.FileNotFoundException;
import com.serviloc.fichiers.domain.exception.FileTooLargeException;
import com.serviloc.fichiers.domain.exception.InvalidFileException;
import com.serviloc.fichiers.domain.exception.UnsupportedFileTypeException;
import com.serviloc.fichiers.infrastructure.storage.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Traduit toutes les exceptions en réponses d'erreur conformes au format unique
 * du contrat API ServiLoc (section 3.2/3.3) :
 * { "success": false, "error": { "code", "message", "field" } }
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFile(InvalidFileException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler({FileTooLargeException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ApiErrorResponse> handleFileTooLarge(Exception ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiErrorResponse.of("FILE_TOO_LARGE", "Fichier trop volumineux"));
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedType(UnsupportedFileTypeException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiErrorResponse.of("UNSUPPORTED_MEDIA_TYPE", ex.getMessage()));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleFileNotFound(FileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of("FILE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("VALIDATION_ERROR",
                        "Champ manquant : " + ex.getRequestPartName(), ex.getRequestPartName()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst();
        String message = fieldError.map(f -> f.getField() + " : " + f.getDefaultMessage())
                .orElse("Données invalides");
        String field = fieldError.map(org.springframework.validation.FieldError::getField).orElse(null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of("VALIDATION_ERROR", message, field));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiErrorResponse> handleStorage(StorageException ex) {
        log.error("Erreur de stockage MinIO", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("INTERNAL_ERROR", "Erreur de stockage du fichier"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        log.error("Erreur inattendue", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of("INTERNAL_ERROR", "Une erreur inattendue est survenue"));
    }
}
