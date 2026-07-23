// domain/exception/InvalidInternalTokenException.java
package com.serviloc.mission.domain.exception;

public class InvalidInternalTokenException extends RuntimeException {
    public InvalidInternalTokenException() {
        super("Token interne invalide ou manquant");
    }
}