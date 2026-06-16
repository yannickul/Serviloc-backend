// PaiementStatsClient.java
package com.serviloc.mission.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "service-paiements", path = "/internal")
public interface PaiementStatsClient {

    @GetMapping("/stats")
    FinancialStatsDto getFinancialStats(
            @RequestParam String from,
            @RequestParam String to
    );

    @PostMapping("/transactions/release")
    void releaseTransaction(@RequestParam String transactionId);
}