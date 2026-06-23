// infrastructure/external/adapter/NegociationClientAdapter.java
package com.serviloc.mission.infrastructure.external.adapter;

import com.serviloc.mission.application.port.out.QuotePort;
import com.serviloc.mission.infrastructure.external.NegociationClient;
import com.serviloc.mission.infrastructure.external.QuoteDto;
import org.springframework.stereotype.Component;

@Component
public class NegociationClientAdapter implements QuotePort {

    private final NegociationClient negociationClient;

    public NegociationClientAdapter(NegociationClient negociationClient) {
        this.negociationClient = negociationClient;
    }

    @Override
    public QuoteDto getQuoteById(String quoteId) {
        return negociationClient.getQuoteById(quoteId);
    }
}