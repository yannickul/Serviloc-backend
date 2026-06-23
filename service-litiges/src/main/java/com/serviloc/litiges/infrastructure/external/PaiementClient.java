// infrastructure/external/PaiementClient.java
package com.serviloc.litiges.infrastructure.external;

import com.serviloc.litiges.infrastructure.external.dto.RefundRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "service-paiement", fallback = PaiementClientFallback.class)
public interface PaiementClient {

    @PatchMapping("/internal/transactions/{transactionId}/freeze")
    void freezeTransaction(@PathVariable String transactionId);

    @PostMapping("/internal/transactions/{transactionId}/refund")
    void refund(@PathVariable String transactionId,
                @RequestBody RefundRequest request);
}