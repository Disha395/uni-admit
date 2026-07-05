package com.example.notification_service.config;

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

    // Queue names — must match Admission Service exactly
    public static final String APPLICATION_SUBMITTED_QUEUE = "q.application.submitted";
    public static final String STATUS_CHANGED_QUEUE        = "q.status.changed";
    public static final String USER_REGISTERED_QUEUE       = "q.user.registered";

    // Notification Service declares the user.registered queue
    // (Admission Service owns the other two — they already exist)
    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(USER_REGISTERED_QUEUE).build();
    }

    // JSON message converter — must match what Admission Service uses
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

    // Configure listener factory to use JSON converter
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        // Retry failed messages up to 3 times before sending to DLQ
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}


