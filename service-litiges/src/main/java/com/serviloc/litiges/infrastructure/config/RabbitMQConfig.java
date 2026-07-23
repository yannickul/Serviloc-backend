// infrastructure/config/RabbitMQConfig.java
package com.serviloc.litiges.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE     = "serviloc.events";
    public static final String LITIGES_QUEUE = "litiges.queue";
    public static final String LITIGES_DLQ   = "litiges.dlq";

    @Bean
    public TopicExchange servilocExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue litigesQueue() {
        return QueueBuilder.durable(LITIGES_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", LITIGES_DLQ)
                .build();
    }

    @Bean
    public Queue litigesDlq() {
        return QueueBuilder.durable(LITIGES_DLQ).build();
    }

    @Bean
    public Binding litigesPaymentReleasedBinding(Queue litigesQueue, TopicExchange servilocExchange) {
        return BindingBuilder.bind(litigesQueue)
                .to(servilocExchange)
                .with("payment.released");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}