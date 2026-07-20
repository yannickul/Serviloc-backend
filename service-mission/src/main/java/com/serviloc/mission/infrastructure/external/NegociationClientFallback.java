package com.serviloc.mission.infrastructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;

@Component
public class NegociationClientFallback implements NegociationClient {

    private static final Logger log = LoggerFactory.getLogger(NegociationClientFallback.class);

    @Override
    public QuoteDto getQuoteById(String quoteId) {
        log.warn("NegociationClient indisponible — quoteId={} non résolu", quoteId);
        return new QuoteDto(quoteId, null, null, null, null, BigDecimal.ZERO,
                Collections.emptyList(), BigDecimal.ZERO, BigDecimal.ZERO, null, null, null, null, null);
    }

    @Override
    public QuoteDto createQuote(NegociationCreateQuoteRequest request) {
        log.error("NegociationClient indisponible — impossible de créer le devis pour demande={}", request.demandId());
        throw new IllegalStateException("Service Négociations indisponible — impossible de créer le devis. Réessayez ultérieurement.");
    }

    @Override
    public QuoteDto updateQuote(String id, NegociationUpdateQuoteRequest request) {
        log.error("NegociationClient indisponible — impossible de mettre à jour le devis {}", id);
        throw new IllegalStateException("Service Négociations indisponible — impossible de modifier le devis. Réessayez ultérieurement.");
    }
}