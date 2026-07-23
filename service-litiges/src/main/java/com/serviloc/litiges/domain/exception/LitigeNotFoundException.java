// domain/exception/LitigeNotFoundException.java
package com.serviloc.litiges.domain.exception;

public class LitigeNotFoundException extends RuntimeException {
    public LitigeNotFoundException(String id) {
        super("Litige introuvable : " + id);
    }
}