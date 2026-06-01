package com.erichiroshi.speechaihexagonal.analysis.infrastructure.messaging.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração central do RabbitMQ.
 *
 * <p>Topologia:
 * <pre>
 *
 * Exchange (summary.events) ─────────► Queue (summary.events.queue)
 *                                          │ on failure (3x)
 *                                          ▼
 *                                     DLQ (summary.events.dlq)
 * </pre>
 *
 * <p>Preparado para consumers de notificação (email/SMS/WhatsApp) na Fase 11.
 */
@Configuration
public class RabbitMqAnalysisConfig {

    // ── Summary ────────────────────────────────────────────────────────────

    public static final String SUMMARY_EXCHANGE    = "summary.events";
    public static final String SUMMARY_QUEUE       = "summary.events.queue";
    public static final String SUMMARY_ROUTING_KEY = "summary.completed";
    public static final String SUMMARY_DLQ         = "summary.events.dlq";
    public static final String SUMMARY_DLX         = "summary.events.dlx";

     // ── Beans — Summary ────────────────────────────────────────────────────

    @Bean
    public TopicExchange summaryExchange() {
        return ExchangeBuilder.topicExchange(SUMMARY_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange summaryDeadLetterExchange() {
        return ExchangeBuilder.directExchange(SUMMARY_DLX).durable(true).build();
    }

    @Bean
    public Queue summaryQueue() {
        return QueueBuilder.durable(SUMMARY_QUEUE)
                .withArgument("x-dead-letter-exchange", SUMMARY_DLX)
                .withArgument("x-dead-letter-routing-key", SUMMARY_DLQ)
                .build();
    }

    @Bean
    public Queue summaryDeadLetterQueue() {
        return QueueBuilder.durable(SUMMARY_DLQ).build();
    }

    @Bean
    public Binding summaryBinding() {
        return BindingBuilder.bind(summaryQueue())
                .to(summaryExchange())
                .with(SUMMARY_ROUTING_KEY);
    }

    @Bean
    public Binding summaryDlqBinding() {
        return BindingBuilder.bind(summaryDeadLetterQueue())
                .to(summaryDeadLetterExchange())
                .with(SUMMARY_DLQ);
    }
}
