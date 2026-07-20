package com.serviloc.mission.application.port.out;

import com.serviloc.mission.infrastructure.external.NegociationCreateQuoteRequest;
import com.serviloc.mission.infrastructure.external.NegociationUpdateQuoteRequest;
import com.serviloc.mission.infrastructure.external.QuoteDto;

public interface QuotePort {
    QuoteDto getQuoteById(String quoteId);
    QuoteDto createQuote(NegociationCreateQuoteRequest request);
    QuoteDto updateQuote(String quoteId, NegociationUpdateQuoteRequest request);
}