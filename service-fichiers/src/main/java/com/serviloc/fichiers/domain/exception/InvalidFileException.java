package com.serviloc.fichiers.domain.exception;

/**
 * Levée quand le fichier ne respecte pas le format attendu (mimeType non autorisé
 * pour le contexte d'upload). Traduite en HTTP 400 VALIDATION_ERROR.
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
