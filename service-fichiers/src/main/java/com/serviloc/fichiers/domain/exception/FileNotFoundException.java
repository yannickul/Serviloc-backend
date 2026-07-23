package com.serviloc.fichiers.domain.exception;

/**
 * Levée quand un fichier n'est trouvé ni en base ni dans le stockage MinIO.
 * Traduite en HTTP 404 (error.code = FILE_NOT_FOUND) par le
 * GlobalExceptionHandler de la couche presentation.
 */
public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String fileId) {
        super("Fichier introuvable : " + fileId);
    }
}
