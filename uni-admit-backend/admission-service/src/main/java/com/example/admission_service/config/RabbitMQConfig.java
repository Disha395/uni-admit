package com.example.admission_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Exchange names ────────────────────────────────────────────────────────
    public static final String ADMISSION_EXCHANGE     = "admission.exchange";
    public static final String NOTIFICATION_EXCHANGE  = "notification.exchange";
    public static final String DLX_EXCHANGE           = "dlx.exchange";

    // ── Queue names ───────────────────────────────────────────────────────────
    public static final String APPLICATION_SUBMITTED_QUEUE = "q.application.submitted";
    public static final String STATUS_CHANGED_QUEUE        = "q.status.changed";
    public static final String PAYMENT_COMPLETED_QUEUE     = "q.payment.completed";
    public static final String DLQ_QUEUE                   = "q.dlq.admission";

    // ── Routing keys ──────────────────────────────────────────────────────────
    public static final String APPLICATION_SUBMITTED_KEY = "application.submitted";
    public static final String STATUS_CHANGED_KEY        = "application.status.changed";
    public static final String PAYMENT_COMPLETED_KEY     = "payment.completed";

    // ── Exchanges ─────────────────────────────────────────────────────────────
    @Bean
    public TopicExchange admissionExchange() {
        return new TopicExchange(ADMISSION_EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange notificationExchange() {
        return new FanoutExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    // ── Queues ────────────────────────────────────────────────────────────────
    @Bean
    public Queue applicationSubmittedQueue() {
        return QueueBuilder.durable(APPLICATION_SUBMITTED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.admission")
                .build();
    }

    @Bean
    public Queue statusChangedQueue() {
        return QueueBuilder.durable(STATUS_CHANGED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.admission")
                .build();
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.admission")
                .build();
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    // ── Bindings ──────────────────────────────────────────────────────────────
    @Bean
    public Binding applicationSubmittedBinding() {
        return BindingBuilder
                .bind(applicationSubmittedQueue())
                .to(admissionExchange())
                .with(APPLICATION_SUBMITTED_KEY);
    }

    @Bean
    public Binding statusChangedBinding() {
        return BindingBuilder
                .bind(statusChangedQueue())
                .to(admissionExchange())
                .with(STATUS_CHANGED_KEY);
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder
                .bind(paymentCompletedQueue())
                .to(admissionExchange())
                .with(PAYMENT_COMPLETED_KEY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(dlqQueue())
                .to(dlxExchange())
                .with("dlq.admission");
    }

    // ── Message converter — JSON instead of Java serialization ────────────────
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
}

