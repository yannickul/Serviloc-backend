// domain/exception/UnauthorizedLitigeAccessException.java
package com.serviloc.litiges.domain.exception;

public class UnauthorizedLitigeAccessException extends RuntimeException {
    public UnauthorizedLitigeAccessException(String message) {
        super(message);
    }
}