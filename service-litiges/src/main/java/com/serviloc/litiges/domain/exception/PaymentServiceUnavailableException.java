// domain/exception/PaymentServiceUnavailableException.java
package com.serviloc.litiges.domain.exception;

public class PaymentServiceUnavailableException extends RuntimeException {
    public PaymentServiceUnavailableException(String message) {
        super(message);
    }
}