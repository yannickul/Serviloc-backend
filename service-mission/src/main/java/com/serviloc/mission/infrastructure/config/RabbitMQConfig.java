// RabbitMQConfig.java
package com.serviloc.mission.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ===== EXCHANGE PRINCIPAL =====
    public static final String EXCHANGE = "serviloc.events";

    // ===== ROUTING KEYS =====
    public static final String RK_DEMAND_PUBLISHED      = "demand.published";
    public static final String RK_QUOTE_ACCEPTED        = "negotiation.quote_accepted";
    public static final String RK_PAYMENT_CONFIRMED     = "payment.confirmed";
    public static final String RK_PAYMENT_FAILED        = "payment.failed";
    public static final String RK_MISSION_VALIDATED     = "mission.validated";
    public static final String RK_MISSION_COMPLETED     = "mission.completed";
    public static final String RK_EVALUATION_CREATED    = "evaluation.created";
    public static final String RK_MISSION_STARTED       = "mission.started";

    // ===== QUEUES =====
    public static final String Q_PAYMENT_CONFIRMED  = "missions.payment.confirmed";
    public static final String Q_PAYMENT_FAILED     = "missions.payment.failed";

    // ===== DLQ =====
    public static final String DLQ_PAYMENT_CONFIRMED = "missions.payment.confirmed.dlq";
    public static final String DLQ_PAYMENT_FAILED    = "missions.payment.failed.dlq";
    public static final String DLX                   = "serviloc.dlx";

    // ===== EXCHANGE PRINCIPAL =====
    @Bean
    public TopicExchange servilocExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    // ===== DEAD LETTER EXCHANGE =====
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    // ===== QUEUES AVEC DLQ =====
    @Bean
    public Queue paymentConfirmedQueue() {
        return QueueBuilder.durable(Q_PAYMENT_CONFIRMED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_PAYMENT_CONFIRMED)
                .build();
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(Q_PAYMENT_FAILED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_PAYMENT_FAILED)
                .build();
    }

    // ===== DLQ =====
    @Bean
    public Queue paymentConfirmedDlq() {
        return QueueBuilder.durable(DLQ_PAYMENT_CONFIRMED).build();
    }

    @Bean
    public Queue paymentFailedDlq() {
        return QueueBuilder.durable(DLQ_PAYMENT_FAILED).build();
    }

    // ===== BINDINGS =====
    @Bean
    public Binding bindPaymentConfirmed() {
        return BindingBuilder
                .bind(paymentConfirmedQueue())
                .to(servilocExchange())
                .with(RK_PAYMENT_CONFIRMED);
    }

    @Bean
    public Binding bindPaymentFailed() {
        return BindingBuilder
                .bind(paymentFailedQueue())
                .to(servilocExchange())
                .with(RK_PAYMENT_FAILED);
    }

    @Bean
    public Binding bindDlqConfirmed() {
        return BindingBuilder
                .bind(paymentConfirmedDlq())
                .to(deadLetterExchange())
                .with(DLQ_PAYMENT_CONFIRMED);
    }

    @Bean
    public Binding bindDlqFailed() {
        return BindingBuilder
                .bind(paymentFailedDlq())
                .to(deadLetterExchange())
                .with(DLQ_PAYMENT_FAILED);
    }

    // ===== SÉRIALISATION JSON =====
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ===== LISTENER FACTORY =====
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}