// infrastructure/external/NegociationClientFallback.java
package com.serviloc.mission.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NegociationClientFallback implements NegociationClient {

    @Override
    public QuoteSummary getQuoteById(String quoteId) {
        log.warn("NegociationClient indisponible — estimatedDurationHours non résolu pour quoteId={}", quoteId);
        return new QuoteSummary(quoteId, 0);
    }
}