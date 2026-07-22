package com.serviloc.mission.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "service-paiements", path = "/internal",
        fallback = PaiementStatsClientFallback.class)
public interface PaiementStatsClient {

    @GetMapping("/stats/financials")
    FinancialStatsDto getFinancialStats(
            @RequestParam String from,
            @RequestParam String to
    );

    @PostMapping("/transactions/{transactionId}/release")
    void releaseTransaction(@PathVariable String transactionId);
}