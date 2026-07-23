package com.serviloc.utilisateurs.infrastructure.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "service-paiement", fallback = PaiementClientFallback.class)
public interface PaiementClient {

    @GetMapping("/internal/transactions/client/{clientId}/pending")
    ClientTransactionSummary getClientTransactionSummary(
            @PathVariable("clientId") String clientId,
            @RequestHeader("X-Internal-Token") String internalToken
    );

    @GetMapping("/provider/earnings")
    ProviderEarningsResponse getProviderEarnings(
            @RequestHeader("X-User-Id") String providerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String month
    );

    record ClientTransactionSummary(
            double totalSpent,
            List<PendingTransactionResponse> pendingTransactions
    ) {}

    record PendingTransactionResponse(
            String id,
            String demandId,
            String clientId,
            String providerId,
            double amount,
            double commissionAmount,
            double netAmount,
            String status,
            String paymentMethod,
            String externalRef,
            String createdAt
    ) {}

    record ProviderEarningsResponse(
            double monthlyTotal,
            List<PayoutResponse> payouts
    ) {}

    record PayoutResponse(
            String id,
            String transactionId,
            double amount,
            double commissionAmount,
            String status,
            String externalRef,
            String createdAt
    ) {}
}