package com.serviloc.negociations.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE          = "serviloc.events";
    public static final String EXCHANGE_DLX      = "serviloc.events.dlx";
    public static final String QUEUE_NEGOTIATIONS = "negotiations.queue";
    public static final String QUEUE_NEGO_DLQ    = "negotiations.dlq";

    @Bean
    TopicExchange servilocEventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange servilocEventsDlx() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLX).durable(true).build();
    }

    @Bean
    Queue negotiationsQueue() {
        return QueueBuilder.durable(QUEUE_NEGOTIATIONS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", QUEUE_NEGO_DLQ)
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    Queue negotiationsDlq() {
        return QueueBuilder.durable(QUEUE_NEGO_DLQ).build();
    }

    @Bean
    Binding bindNegotiationsPaymentFailed(Queue negotiationsQueue,
                                          TopicExchange servilocEventsExchange) {
        return BindingBuilder.bind(negotiationsQueue)
                .to(servilocEventsExchange).with("payment.failed");
    }

    @Bean
    Binding bindNegotiationsDlq(Queue negotiationsDlq,
                                DirectExchange servilocEventsDlx) {
        return BindingBuilder.bind(negotiationsDlq)
                .to(servilocEventsDlx).with(QUEUE_NEGO_DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    @Primary
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }
}