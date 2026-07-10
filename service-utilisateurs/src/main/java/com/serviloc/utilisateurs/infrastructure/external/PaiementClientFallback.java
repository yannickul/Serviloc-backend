package com.serviloc.utilisateurs.infrastructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaiementClientFallback implements PaiementClient {

    private static final Logger log = LoggerFactory.getLogger(PaiementClientFallback.class);

    @Override
    public ClientTransactionSummary getClientTransactionSummary(String clientId,
                                                                String internalToken) {
        log.warn("[FEIGN FALLBACK] Service Paiement indisponible pour clientId={}", clientId);
        return new ClientTransactionSummary(0.0, List.of());
    }

    @Override
    public ProviderEarningsResponse getProviderEarnings(String providerId,
                                                        int page, String month) {
        log.warn("[FEIGN FALLBACK] Service Paiement indisponible pour providerId={}", providerId);
        return new ProviderEarningsResponse(0.0, List.of());
    }
}