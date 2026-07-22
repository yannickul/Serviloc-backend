package com.serviloc.negociations.domain.exception;

/** Devis introuvable pour un couple (demandId, providerId) précis — mappée en 404. */
public class QuoteNotFoundException extends RuntimeException {
    public QuoteNotFoundException(String message) {
        super(message);
    }
}
