// infrastructure/messaging/LitigeEventConsumer.java
package com.serviloc.litiges.infrastructure.messaging;

import com.serviloc.litiges.application.service.AdminLitigeService;
import com.serviloc.litiges.infrastructure.config.RabbitMQConfig;
import com.serviloc.litiges.infrastructure.messaging.dto.PaymentReleasedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LitigeEventConsumer {

    private final AdminLitigeService adminLitigeService;

    @RabbitListener(queues = RabbitMQConfig.LITIGES_QUEUE)
    public void onPaymentReleased(PaymentReleasedEvent event) {
        log.info("[CONSUMER] payment.released reçu — transactionId={}", event.transactionId());

        if (event.transactionId() == null || event.transactionId().isBlank()) {
            log.warn("[CONSUMER] transactionId absent dans payment.released — event ignoré");
            return;
        }

        try {
            adminLitigeService.closeByTransactionId(event.transactionId());
        } catch (Exception e) {
            log.error("[CONSUMER] Erreur fermeture litige pour transactionId={} — envoi en DLQ",
                    event.transactionId(), e);
            throw e;
        }
    }
}