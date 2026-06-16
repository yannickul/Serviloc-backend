// infrastructure/external/adapter/PaiementClientAdapter.java
package com.serviloc.mission.infrastructure.external.adapter;

import com.serviloc.mission.application.port.out.PaymentPort;
import com.serviloc.mission.infrastructure.external.PaiementStatsClient;
import org.springframework.stereotype.Component;

@Component
public class PaiementClientAdapter implements PaymentPort {

    private final PaiementStatsClient paiementStatsClient;

    public PaiementClientAdapter(PaiementStatsClient paiementStatsClient) {
        this.paiementStatsClient = paiementStatsClient;
    }

    @Override
    public void releaseTransaction(String transactionId) {
        paiementStatsClient.releaseTransaction(transactionId);
    }
}