// domain/exception/LitigeAlreadyResolvedException.java
package com.serviloc.litiges.domain.exception;

public class LitigeAlreadyResolvedException extends RuntimeException {
    public LitigeAlreadyResolvedException(String id) {
        super("Ce litige est déjà résolu : " + id);
    }
}