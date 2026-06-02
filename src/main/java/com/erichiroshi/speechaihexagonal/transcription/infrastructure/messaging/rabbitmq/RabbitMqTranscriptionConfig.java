package com.erichiroshi.speechaihexagonal.transcription.infrastructure.messaging.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Configuração central do RabbitMQ.
 *
 * <p>Topologia:
 * <pre>
 * Exchange (transcription.events) ──► Queue (transcription.events.queue)
 *                                          │ on failure (3x)
 *                                          ▼
 *                                     DLQ (transcription.events.dlq)
 * </pre>
 *
 * <p>Preparado para consumers de notificação (email/SMS/WhatsApp) na Fase 11.
 */
@Configuration
public class RabbitMqTranscriptionConfig {

    // ── Transcription ──────────────────────────────────────────────────────

    public static final String TRANSCRIPTION_EXCHANGE       = "transcription.events";
    public static final String TRANSCRIPTION_QUEUE          = "transcription.events.queue";
    public static final String TRANSCRIPTION_ROUTING_KEY    = "transcription.completed";
    public static final String TRANSCRIPTION_DLQ            = "transcription.events.dlq";
    public static final String TRANSCRIPTION_DLX            = "transcription.events.dlx";

    // ── Beans — Transcription ──────────────────────────────────────────────

    @Bean
    public TopicExchange transcriptionExchange() {
        return ExchangeBuilder.topicExchange(TRANSCRIPTION_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange transcriptionDeadLetterExchange() {
        return ExchangeBuilder.directExchange(TRANSCRIPTION_DLX).durable(true).build();
    }

    @Bean
    public Queue transcriptionQueue() {
        return QueueBuilder.durable(TRANSCRIPTION_QUEUE)
                .withArgument("x-dead-letter-exchange", TRANSCRIPTION_DLX)
                .withArgument("x-dead-letter-routing-key", TRANSCRIPTION_DLQ)
                .build();
    }

    @Bean
    public Queue transcriptionDeadLetterQueue() {
        return QueueBuilder.durable(TRANSCRIPTION_DLQ).build();
    }

    @Bean
    public Binding transcriptionBinding() {
        return BindingBuilder.bind(transcriptionQueue())
                .to(transcriptionExchange())
                .with(TRANSCRIPTION_ROUTING_KEY);
    }

    @Bean
    public Binding transcriptionDlqBinding() {
        return BindingBuilder.bind(transcriptionDeadLetterQueue())
                .to(transcriptionDeadLetterExchange())
                .with(TRANSCRIPTION_DLQ);
    }
}
