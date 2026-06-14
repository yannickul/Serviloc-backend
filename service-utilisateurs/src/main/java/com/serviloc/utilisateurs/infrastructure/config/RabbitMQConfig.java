package com.serviloc.utilisateurs.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ─── Exchange names ───────────────────────────────────────────
    public static final String EXCHANGE     = "serviloc.events";
    public static final String EXCHANGE_DLX = "serviloc.events.dlx";

    // ─── Queue names ──────────────────────────────────────────────
    public static final String QUEUE_PAYMENT       = "payment.queue";
    public static final String QUEUE_MISSIONS      = "missions.queue";
    public static final String QUEUE_NEGOTIATIONS  = "negotiations.queue";
    public static final String QUEUE_NOTIFICATIONS = "notifications.queue";
    public static final String QUEUE_CATEGORIES    = "categories.queue";

    // ─── Routing keys émis par ce service ────────────────────────
    public static final String RK_USER_REGISTERED    = "user.registered";
    public static final String RK_PROVIDER_VALIDATED = "provider.validated";
    public static final String RK_PROVIDER_REJECTED  = "provider.rejected";
    public static final String RK_USER_SUSPENDED     = "user.suspended";

    // ─── Exchanges ────────────────────────────────────────────────

    @Bean
    TopicExchange servilocEventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange servilocEventsDlx() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLX).durable(true).build();
    }

    // ─── Queues avec DLQ ─────────────────────────────────────────

    @Bean
    Queue paymentQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "payment.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    Queue missionsQueue() {
        return QueueBuilder.durable(QUEUE_MISSIONS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "missions.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    Queue negotiationsQueue() {
        return QueueBuilder.durable(QUEUE_NEGOTIATIONS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "negotiations.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    Queue notificationsQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATIONS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "notifications.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    Queue categoriesQueue() {
        return QueueBuilder.durable(QUEUE_CATEGORIES)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", "categories.dlq")
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    // ─── Bindings ─────────────────────────────────────────────────

    @Bean Binding bindPaymentQuoteAccepted(Queue paymentQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(paymentQueue).to(servilocEventsExchange).with("negotiation.quote.accepted");
    }

    @Bean Binding bindPaymentQuoteRefused(Queue paymentQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(paymentQueue).to(servilocEventsExchange).with("negotiation.quote.refused");
    }

    @Bean Binding bindPaymentMissionCompleted(Queue paymentQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(paymentQueue).to(servilocEventsExchange).with("mission.completed");
    }

    @Bean Binding bindMissionsPaymentConfirmed(Queue missionsQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(missionsQueue).to(servilocEventsExchange).with("payment.confirmed");
    }

    @Bean Binding bindMissionsPaymentFailed(Queue missionsQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(missionsQueue).to(servilocEventsExchange).with("payment.failed");
    }

    @Bean Binding bindNegotiationsPaymentFailed(Queue negotiationsQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(negotiationsQueue).to(servilocEventsExchange).with("payment.failed");
    }

    @Bean Binding bindNotificationsUser(Queue notificationsQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(servilocEventsExchange).with("user.*");
    }

    @Bean Binding bindNotificationsProvider(Queue notificationsQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(servilocEventsExchange).with("provider.*");
    }

    @Bean Binding bindNotificationsPayment(Queue notificationsQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(servilocEventsExchange).with("payment.*");
    }

    @Bean Binding bindNotificationsNegotiation(Queue notificationsQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(servilocEventsExchange).with("negotiation.*");
    }

    @Bean Binding bindNotificationsMission(Queue notificationsQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(servilocEventsExchange).with("mission.*");
    }

    @Bean Binding bindNotificationsLitige(Queue notificationsQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(servilocEventsExchange).with("litige.*");
    }

    @Bean Binding bindNotificationsDemand(Queue notificationsQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(servilocEventsExchange).with("demand.*");
    }

    @Bean Binding bindCategoriesDemand(Queue categoriesQueue, TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(categoriesQueue).to(servilocEventsExchange).with("demand.published");
    }

    // ─── Converter JSON + RabbitTemplate ─────────────────────────

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}