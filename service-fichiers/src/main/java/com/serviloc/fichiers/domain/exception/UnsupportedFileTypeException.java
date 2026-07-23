package com.serviloc.fichiers.domain.exception;

/**
 * Levée quand le type réel du fichier (détecté par analyse du contenu, pas de
 * l'extension) est un type dangereux ou non supporté (ex: exécutable .exe
 * envoyé sous une extension trompeuse). Traduite en HTTP 415 UNSUPPORTED_MEDIA_TYPE.
 */
public class UnsupportedFileTypeException extends RuntimeException {

    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}
