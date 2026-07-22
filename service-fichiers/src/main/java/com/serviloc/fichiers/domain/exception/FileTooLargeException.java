package com.serviloc.fichiers.domain.exception;

/**
 * Levée quand la taille du fichier dépasse la limite autorisée (5 Mo photos,
 * 10 Mo documents). Traduite en HTTP 413 FILE_TOO_LARGE.
 */
public class FileTooLargeException extends RuntimeException {

    public FileTooLargeException(String message) {
        super(message);
    }
}
