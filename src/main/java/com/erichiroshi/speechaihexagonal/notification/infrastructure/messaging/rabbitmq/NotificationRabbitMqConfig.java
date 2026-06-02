package com.erichiroshi.speechaihexagonal.notification.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.analysis.infrastructure.messaging.rabbitmq.RabbitMqAnalysisConfig;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.messaging.rabbitmq.RabbitMqTranscriptionConfig;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração das bindings de notificação.
 *
 * <p>O contexto notification/ assina os exchanges de transcription e summary
 * com suas próprias filas — sem acoplar ao RabbitMqConfig do contexto transcription/.
 * Cada bounded context tem sua própria fila de consumo no mesmo exchange.
 */
@Configuration
public class NotificationRabbitMqConfig {

    // Filas de consumo do bounded context notification/
    public static final String NOTIFICATION_TRANSCRIPTION_QUEUE = "notification.transcription.queue";
    public static final String NOTIFICATION_SUMMARY_QUEUE       = "notification.summary.queue";
    public static final String NOTIFICATION_TRANSCRIPTION_DLQ   = "notification.transcription.dlq";
    public static final String NOTIFICATION_SUMMARY_DLQ         = "notification.summary.dlq";
    public static final String NOTIFICATION_DLX                 = "notification.events.dlx";

    @Bean
    public DirectExchange notificationDeadLetterExchange() {
        return ExchangeBuilder.directExchange(NOTIFICATION_DLX).durable(true).build();
    }

    @Bean
    public Queue notificationTranscriptionQueue() {
        return QueueBuilder.durable(NOTIFICATION_TRANSCRIPTION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_TRANSCRIPTION_DLQ)
                .build();
    }

    @Bean
    public Queue notificationSummaryQueue() {
        return QueueBuilder.durable(NOTIFICATION_SUMMARY_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLX)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_SUMMARY_DLQ)
                .build();
    }

    @Bean
    public Queue notificationTranscriptionDlq() {
        return QueueBuilder.durable(NOTIFICATION_TRANSCRIPTION_DLQ).build();
    }

    @Bean
    public Queue notificationSummaryDlq() {
        return QueueBuilder.durable(NOTIFICATION_SUMMARY_DLQ).build();
    }

    // Bindings — notification/ assina os exchanges dos outros contextos
    @Bean
    public Binding notificationTranscriptionBinding(
            Queue notificationTranscriptionQueue,
            TopicExchange transcriptionExchange) {
        return BindingBuilder.bind(notificationTranscriptionQueue)
                .to(transcriptionExchange)
                .with(RabbitMqTranscriptionConfig.TRANSCRIPTION_ROUTING_KEY);
    }

    @Bean
    public Binding notificationSummaryBinding(
            Queue notificationSummaryQueue,
            TopicExchange summaryExchange) {
        return BindingBuilder.bind(notificationSummaryQueue)
                .to(summaryExchange)
                .with(RabbitMqAnalysisConfig.SUMMARY_ROUTING_KEY);
    }

    @Bean
    public Binding notificationTranscriptionDlqBinding(
            Queue notificationTranscriptionDlq,
            DirectExchange notificationDeadLetterExchange) {
        return BindingBuilder.bind(notificationTranscriptionDlq)
                .to(notificationDeadLetterExchange)
                .with(NOTIFICATION_TRANSCRIPTION_DLQ);
    }

    @Bean
    public Binding notificationSummaryDlqBinding(
            Queue notificationSummaryDlq,
            DirectExchange notificationDeadLetterExchange) {
        return BindingBuilder.bind(notificationSummaryDlq)
                .to(notificationDeadLetterExchange)
                .with(NOTIFICATION_SUMMARY_DLQ);
    }
}
