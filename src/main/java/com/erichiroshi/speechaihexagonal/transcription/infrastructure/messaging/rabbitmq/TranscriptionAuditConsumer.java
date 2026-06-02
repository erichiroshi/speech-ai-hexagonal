package com.erichiroshi.speechaihexagonal.transcription.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.transcription.domain.event.TranscriptionCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer de auditoria do contexto transcription/.
 *
 * <p>Responsabilidade: logging de auditoria dos eventos de transcrição.
 * Notificações ao usuário são responsabilidade do bounded context notification/,
 * que possui sua própria fila no mesmo exchange.
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
    }
}
