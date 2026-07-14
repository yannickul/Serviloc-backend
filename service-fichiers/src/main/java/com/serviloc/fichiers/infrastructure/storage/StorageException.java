package com.serviloc.fichiers.infrastructure.storage;

/**
 * Exception technique levée en cas d'échec de communication avec MinIO.
 * Traduite en HTTP 500 INTERNAL_ERROR par le GlobalExceptionHandler.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
