// MissionEventConsumer.java
package com.serviloc.mission.infrastructure.messaging;

import com.rabbitmq.client.Channel;
import com.serviloc.mission.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class MissionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(MissionEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.Q_PAYMENT_CONFIRMED)
    public void onPaymentConfirmed(
            Map<String, Object> payload,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.info("Reçu payment.confirmed : {}", payload);
        // Sprint 2 : créer Mission + mettre demande EN_COURS
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = RabbitMQConfig.Q_PAYMENT_FAILED)
    public void onPaymentFailed(
            Map<String, Object> payload,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        log.info("Reçu payment.failed : {}", payload);
        // Sprint 2 : remettre demande OUVERTE (compensation Saga1)
        channel.basicAck(tag, false);
    }
}