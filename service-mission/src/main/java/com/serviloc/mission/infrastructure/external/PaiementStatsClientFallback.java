package com.serviloc.mission.infrastructure.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaiementStatsClientFallback implements PaiementStatsClient {

    private static final Logger log = LoggerFactory.getLogger(PaiementStatsClientFallback.class);

    @Override
    public FinancialStatsDto getFinancialStats(String from, String to) {
        log.warn("PaiementStatsClient indisponible — getFinancialStats fallback");
        return new FinancialStatsDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Override
    public void releaseTransaction(String transactionId) {
        // Opération financière critique — on ne peut pas ignorer silencieusement.
        // La mission reste EN_COURS en base, retry manuel ou outbox en Sprint 3.
        log.error("PaiementStatsClient indisponible — releaseTransaction({}) ÉCHOUÉ. Fonds non libérés.", transactionId);
        throw new IllegalStateException(
                "Service Paiement indisponible — impossible de libérer la transaction " + transactionId +
                        ". La mission sera libérée manuellement ou lors de la prochaine tentative.");
    }
}