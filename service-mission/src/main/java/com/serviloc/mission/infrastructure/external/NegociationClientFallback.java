// infrastructure/external/NegociationClientFallback.java
package com.serviloc.mission.infrastructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NegociationClientFallback implements NegociationClient {

    private static final Logger log = LoggerFactory.getLogger(NegociationClientFallback.class);

    @Override
    public QuoteDto getQuoteById(String quoteId) {
        log.warn("NegociationClient indisponible — quoteId={} non résolu", quoteId);
        return new QuoteDto(quoteId, null, null, BigDecimal.ZERO, null, null, null);
    }
}