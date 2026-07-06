// application/port/out/QuotePort.java
package com.serviloc.mission.application.port.out;

import com.serviloc.mission.infrastructure.external.QuoteDto;

public interface QuotePort {
    QuoteDto getQuoteById(String quoteId);
}