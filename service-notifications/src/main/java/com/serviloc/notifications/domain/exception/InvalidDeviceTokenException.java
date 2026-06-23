package com.serviloc.notifications.domain.exception;

/**
 * Levée lorsqu'un token FCM ou un userId fourni ne respecte pas les invariants métier.
 */
public class InvalidDeviceTokenException extends DomainException {

    public InvalidDeviceTokenException(String message) {
        super(message);
    }
}
