package com.serviloc.mission.infrastructure.external.adapter;

import com.serviloc.mission.application.port.out.QuotePort;
import com.serviloc.mission.infrastructure.external.*;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Override
    public QuoteDto createQuote(NegociationCreateQuoteRequest request) {
        return negociationClient.createQuote(request);
    }

    @Override
    public QuoteDto updateQuote(String quoteId, NegociationUpdateQuoteRequest request) {
        return negociationClient.updateQuote(quoteId, request);
    }

    @Override
    public QuoteDto updateQuoteStatus(String quoteId, NegociationUpdateQuoteStatusRequest request) {
        return negociationClient.updateQuoteStatus(quoteId, request);
    }

    @Override
    public List<QuoteDto> getQuotesByDemand(String demandId) {
        return negociationClient.getQuotesByDemand(demandId);
    }

    @Override
    public QuoteDto getQuoteByDemandAndProvider(String demandId, String providerId) {
        return negociationClient.getQuoteByDemandAndProvider(demandId, providerId);
    }
}