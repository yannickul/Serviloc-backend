package com.serviloc.mission.infrastructure.external.adapter;

import com.serviloc.mission.application.port.out.PaymentPort;
import com.serviloc.mission.infrastructure.external.PaiementStatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PaiementClientAdapter implements PaymentPort {

    private static final Logger log = LoggerFactory.getLogger(PaiementClientAdapter.class);
    private final PaiementStatsClient paiementStatsClient;

    public PaiementClientAdapter(PaiementStatsClient paiementStatsClient) {
        this.paiementStatsClient = paiementStatsClient;
    }

    @Override
    public void releaseTransaction(String transactionId) {
        log.info("Libération de la transaction : {}", transactionId);
        try {
            paiementStatsClient.releaseTransaction(transactionId);
            log.info("Transaction {} libérée avec succès", transactionId);
        } catch (Exception e) {
            // Service Paiement indisponible — opération financière critique.
            // On lève une IllegalStateException explicite catchée par GlobalExceptionHandler
            // → 409 BUSINESS_RULE_VIOLATION au lieu de 500 INTERNAL_ERROR.
            // TODO Sprint 3 : implémenter outbox pour retry automatique.
            log.error("Impossible de libérer la transaction {} : {}", transactionId, e.getMessage());
            throw new IllegalStateException(
                    "Service Paiement indisponible — impossible de libérer la transaction " +
                            transactionId + ". Réessayez ultérieurement.");
        }
    }
}