package com.erichiroshi.speechaihexagonal.transcription.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.transcription.domain.event.TranscriptionCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer de auditoria — loga eventos de transcrição recebidos.
 *
 * <p>Base pronta para Fase 11: substituir log por chamada ao
 * NotificationPort (email/SMS/WhatsApp).
 *
 * <p>DLQ ativa: mensagens que falharem 3x são encaminhadas
 * para {@code transcription.events.dlq}.
 */
@Slf4j
@Component
public class TranscriptionAuditConsumer {

    @RabbitListener(
            queues = RabbitMqTranscriptionConfig.TRANSCRIPTION_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void onTranscriptionCompleted(TranscriptionCompletedEvent event) {
        log.info("[AUDIT] TranscriptionCompleted | audioHash={} | fromCache={} | fileName={} | chars={}",
                event.audioHash(),
                event.fromCache(),
                event.fileName(),
                event.text() != null ? event.text().length() : 0);
        // TODO Fase 11: notificationPort.notify(event)
    }
}
