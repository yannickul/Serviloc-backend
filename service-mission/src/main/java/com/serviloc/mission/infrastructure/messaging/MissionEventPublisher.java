// infrastructure/messaging/MissionEventPublisher.java
package com.serviloc.mission.infrastructure.messaging;

import com.serviloc.mission.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MissionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MissionEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public MissionEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishDemandPublished(Object event) {
        log.info("Publication demand.published : {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_DEMAND_PUBLISHED, event);
    }

    public void publishQuoteAccepted(Object event) {
        log.info("Publication negotiation.quote_accepted : {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_QUOTE_ACCEPTED, event);
    }

    public void publishMissionStarted(Object event) {
        log.info("Publication mission.started : {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_MISSION_STARTED, event);
    }

    public void publishMissionValidated(Object event) {
        log.info("Publication mission.validated : {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_MISSION_VALIDATED, event);
    }

    public void publishMissionCompleted(Object event) {
        log.info("Publication mission.completed : {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_MISSION_COMPLETED, event);
    }

    public void publishEvaluationCreated(Object event) {
        log.info("Publication evaluation.created : {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_EVALUATION_CREATED, event);
    }
}