// infrastructure/messaging/MissionEventConsumer.java
package com.serviloc.mission.infrastructure.messaging;

import com.rabbitmq.client.Channel;
import com.serviloc.mission.domain.event.PaymentConfirmedEvent;
import com.serviloc.mission.domain.event.PaymentFailedEvent;
import com.serviloc.mission.domain.model.*;
import com.serviloc.mission.domain.repository.DemandRepository;
import com.serviloc.mission.domain.repository.MissionRepository;
import com.serviloc.mission.infrastructure.config.RabbitMQConfig;
import com.serviloc.mission.infrastructure.external.NegociationClient;
import com.serviloc.mission.infrastructure.external.QuoteSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
public class MissionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MissionEventConsumer.class);

    private final DemandRepository demandRepository;
    private final MissionRepository missionRepository;
    private final NegociationClient negociationClient;

    public MissionEventConsumer(
            DemandRepository demandRepository,
            MissionRepository missionRepository,
            NegociationClient negociationClient) {
        this.demandRepository = demandRepository;
        this.missionRepository = missionRepository;
        this.negociationClient = negociationClient;
    }

    @RabbitListener(queues = RabbitMQConfig.Q_PAYMENT_CONFIRMED)
    @Transactional
    public void onPaymentConfirmed(
            PaymentConfirmedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.info("Reçu payment.confirmed pour demande={} transaction={}", event.demandId(), event.transactionId());

        try {
            Demand demand = demandRepository.findById(event.demandId()).orElse(null);
            if (demand == null) {
                log.error("payment.confirmed reçu pour une demande inconnue : {}", event.demandId());
                channel.basicAck(tag, false);
                return;
            }

            if (demand.getStatus() == DemandStatus.EN_COURS) {
                log.warn("Idempotence : payment.confirmed déjà traité pour demande={}", event.demandId());
                channel.basicAck(tag, false);
                return;
            }

            // quoteId et category ne sont plus transmis par payment.confirmed —
            // récupérés depuis la Demand, déjà renseignés lors de acceptQuote()
            String quoteId = demand.getQuoteId();
            String category = demand.getCategoryId();

            if (quoteId == null) {
                log.error("Demande {} n'a pas de quoteId renseigné — payment.confirmed incohérent", event.demandId());
                channel.basicNack(tag, false, false);
                return;
            }

            Mission mission = new Mission();
            mission.setId("msn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
            mission.setDemandId(event.demandId());
            mission.setQuoteId(quoteId);
            mission.setTransactionId(event.transactionId());
            mission.setClientId(event.clientId());
            mission.setProviderId(event.providerId());
            mission.setCategory(category);
            mission.setStatus(MissionStatus.EN_ATTENTE);

            // ⚠️ TODO Priorité 0 : totalAmount et sequesteredAmount utilisent tous deux
            // event.amount() en attendant confirmation Yannick — à corriger s'il précise
            // deux valeurs distinctes.
            mission.setTotalAmount(event.amount());
            mission.setSequesteredAmount(event.amount());
            mission.setPaymentStatus("CONFIRMED");

            QuoteSummary quote = negociationClient.getQuoteById(quoteId);
            mission.setEstimatedDurationHours(quote.estimatedDurationHours());
            mission.setEstimatedDurationHours(0);

            missionRepository.save(mission);
            log.info("Mission créée : id={} pour demande={}", mission.getId(), event.demandId());

            demand.setStatus(DemandStatus.EN_COURS);
            demand.setProviderId(event.providerId());
            demand .setMissionId(mission.getId());
            demandRepository.save(demand);
            log.info("Demande {} passée en EN_COURS", event.demandId());

            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Erreur traitement payment.confirmed pour demande={} : {}", event.demandId(), e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.Q_PAYMENT_FAILED)
    @Transactional
    public void onPaymentFailed(
            PaymentFailedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.info("Reçu payment.failed pour demande={} raison={}", event.demandId(), event.reason());

        try {
            Demand demand = demandRepository.findById(event.demandId()).orElse(null);
            if (demand == null) {
                log.error("payment.failed reçu pour une demande inconnue : {}", event.demandId());
                channel.basicAck(tag, false);
                return;
            }

            // Idempotence : compensation uniquement si la demande est encore EN_COURS
            // (si elle est déjà OUVERTE, un précédent payment.failed a déjà compensé)
            if (demand.getStatus() != DemandStatus.EN_COURS) {
                log.warn("Idempotence : payment.failed ignoré, demande={} déjà en status={}",
                        event.demandId(), demand.getStatus());
                channel.basicAck(tag, false);
                return;
            }

            demand.setStatus(DemandStatus.OUVERTE);
            demand.setProviderId(null);
            demand.setQuoteId(null);
            demandRepository.save(demand);
            log.info("Compensation Saga1 : demande {} remise en OUVERTE", event.demandId());

            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("Erreur traitement payment.failed pour demande={} : {}", event.demandId(), e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }
}