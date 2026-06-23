package com.serviloc.notifications.domain.exception;

/**
 * Exception de base pour toute violation d'invariant métier du domaine Notifications.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
