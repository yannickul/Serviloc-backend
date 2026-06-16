// NegociationClient.java
package com.serviloc.mission.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "service-negociations", path = "/internal")
public interface NegociationClient {

    @GetMapping("/quotes/{quoteId}")
    QuoteDto getQuoteById(@PathVariable String quoteId);

    @PostMapping("/quotes")
    QuoteDto createQuote(@RequestBody CreateQuoteRequest request);
}