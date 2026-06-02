package com.erichiroshi.speechaihexagonal.notification.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.analysis.domain.event.SummaryCompletedEvent;
import com.erichiroshi.speechaihexagonal.notification.application.port.in.SendNotificationPort;
import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationType;
import com.erichiroshi.speechaihexagonal.transcription.domain.event.TranscriptionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Input Adapter de mensageria — consome eventos de transcrição e resumo,
 * converte em {@link Notification} e delega ao {@link SendNotificationPort}.
 *
 * <p>Ponto único de entrada para notificações orientadas a eventos.
 * O use case não conhece RabbitMQ — apenas recebe Notification.
 *
 * <p>Canais habilitados via {@code app.notification.channels.*}.
 * DLQ ativa: falhas após 3x encaminhadas para {@code notification.*.dlq}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(NotificationProperties.class)
public class NotificationEventConsumer {

    private final SendNotificationPort    sendNotificationPort;
    private final NotificationProperties  properties;

    @RabbitListener(queues = NotificationRabbitMqConfig.NOTIFICATION_TRANSCRIPTION_QUEUE)
    public void onTranscriptionCompleted(TranscriptionCompletedEvent event) {
        log.info("[NOTIFICATION] TranscriptionCompleted recebido | audioHash={}", event.audioHash());

        String message = buildTranscriptionMessage(event);
        sendToEnabledChannels(event.audioHash(), "Transcrição concluída", message, NotificationType.TRANSCRIPTION_COMPLETED);
    }

    @RabbitListener(queues = NotificationRabbitMqConfig.NOTIFICATION_SUMMARY_QUEUE)
    public void onSummaryCompleted(SummaryCompletedEvent event) {
        log.info("[NOTIFICATION] SummaryCompleted recebido | audioHash={}", event.audioHash());

        String message = buildSummaryMessage(event);
        sendToEnabledChannels(event.audioHash(), "Resumo gerado", message, NotificationType.SUMMARY_COMPLETED);
    }

    private void sendToEnabledChannels(String audioHash, String subject, String message, NotificationType type) {

        if (properties.channels().no_op() && hasRecipient(properties.no_opRecipient())) {
            sendNotificationPort.execute(Notification.create(
                    properties.no_opRecipient(), subject, message,
                    NotificationChannel.NO_OP, type, audioHash));
        }

        if (properties.channels().email() && hasRecipient(properties.emailRecipient())) {
            sendNotificationPort.execute(Notification.create(
                    properties.emailRecipient(), subject, message,
                    NotificationChannel.EMAIL, type, audioHash));
        }

        if (properties.channels().sms() && hasRecipient(properties.smsRecipient())) {
            sendNotificationPort.execute(Notification.create(
                    properties.smsRecipient(), subject, message,
                    NotificationChannel.SMS, type, audioHash));
        }

        if (properties.channels().whatsapp() && hasRecipient(properties.whatsappRecipient())) {
            sendNotificationPort.execute(Notification.create(
                    properties.whatsappRecipient(), subject, message,
                    NotificationChannel.WHATSAPP, type, audioHash));
        }
    }

    private boolean hasRecipient(String recipient) {
        return recipient != null && !recipient.isBlank();
    }

    private String buildTranscriptionMessage(TranscriptionCompletedEvent event) {
        return """
                Sua transcrição de áudio foi concluída com sucesso.

                Arquivo: %s
                Hash: %s
                Reutilizada do cache: %s
                Prévia: %s
                """.formatted(
                event.fileName(),
                event.audioHash(),
                event.fromCache() ? "Sim" : "Não",
                truncate(event.text(), 200));
    }

    private String buildSummaryMessage(SummaryCompletedEvent event) {
        return """
                O resumo da sua transcrição foi gerado com sucesso.

                Hash: %s
                Modelo: %s
                Reutilizado do cache: %s

                Resumo:
                %s
                """.formatted(
                event.audioHash(),
                event.model(),
                event.fromCache() ? "Sim" : "Não",
                event.summaryText());
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
