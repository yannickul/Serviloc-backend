package com.serviloc.negociations.infrastructure.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serviloc.negociations.application.service.QuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NegociationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NegociationEventConsumer.class);

    private final QuoteService quoteService;
    private final ObjectMapper objectMapper;

    public NegociationEventConsumer(QuoteService quoteService, ObjectMapper objectMapper) {
        this.quoteService = quoteService;
        this.objectMapper = objectMapper;
    }

    // ─── Consumer negotiations.queue ─────────────────────────────
    // Écoute payment.failed → remet le devis en EN_ATTENTE

    @RabbitListener(queues = "negotiations.queue")
    public void onEvent(byte[] rawBytes) {
        try {
            String raw = new String(rawBytes, java.nio.charset.StandardCharsets.UTF_8);
            log.info("[NEGO-CONSUMER] Message reçu : {}", raw);

            Map<String, Object> event = objectMapper.readValue(
                    raw, new TypeReference<>() {});

            String eventType = (String) event.get("eventType");
            log.info("[NEGO-CONSUMER] eventType={}", eventType);

            if ("payment.failed".equals(eventType)) {
                Map<String, Object> payload = (Map<String, Object>) event.get("payload");
                UUID demandId = UUID.fromString((String) payload.get("demandId"));
                log.info("[NEGO-CONSUMER] payment.failed → reset devis : demandId={}", demandId);
                quoteService.resetQuoteOnPaymentFailed(demandId);
            }

        } catch (Exception e) {
            log.error("[NEGO-CONSUMER] Erreur : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}