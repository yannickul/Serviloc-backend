// infrastructure/external/PaiementClientFallback.java
package com.serviloc.litiges.infrastructure.external;

import com.serviloc.litiges.domain.exception.PaymentServiceUnavailableException;
import com.serviloc.litiges.infrastructure.external.dto.RefundRequest;
import com.serviloc.litiges.infrastructure.external.dto.TransactionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaiementClientFallback implements PaiementClient {

    @Override
    public TransactionDto freezeTransaction(String transactionId) {
        log.error("[FALLBACK] Impossible de geler la transaction {} — Service Paiement indisponible", transactionId);
        // Ne bloque pas la création du litige — le gel sera rejoué via retry ou DLQ (Sprint 4)
        return null;
    }

    @Override
    public TransactionDto refund(String transactionId, RefundRequest request) {
        log.error("[FALLBACK] Remboursement impossible pour {} — Service Paiement indisponible", transactionId);
        throw new PaymentServiceUnavailableException(
                "Service Paiement indisponible — remboursement différé"
        );
    }
}
