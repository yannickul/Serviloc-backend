// domain/exception/LitigeAlreadyAssignedException.java
package com.serviloc.litiges.domain.exception;

public class LitigeAlreadyAssignedException extends RuntimeException {
    public LitigeAlreadyAssignedException(String id) {
        super("Ce litige est déjà assigné : " + id);
    }
}