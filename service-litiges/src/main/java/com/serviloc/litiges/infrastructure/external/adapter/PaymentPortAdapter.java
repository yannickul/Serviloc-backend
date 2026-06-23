// infrastructure/external/PaymentPortAdapter.java
package com.serviloc.litiges.infrastructure.external.adapter;

import com.serviloc.litiges.application.port.out.PaymentPort;
import com.serviloc.litiges.infrastructure.external.PaiementClient;
import com.serviloc.litiges.infrastructure.external.dto.RefundRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentPortAdapter implements PaymentPort {

    private final PaiementClient paiementClient;

    @Override
    public void freezeTransaction(String transactionId) {
        log.info("[FEIGN] Gel transaction — transactionId={}", transactionId);
        paiementClient.freezeTransaction(transactionId);
    }

    @Override
    public void refund(String transactionId, BigDecimal amount, String reason) {
        log.info("[FEIGN] Remboursement — transactionId={} amount={}", transactionId, amount);
        paiementClient.refund(transactionId, new RefundRequest(amount, reason));
    }
}