package com.erichiroshi.speechaihexagonal.transcription.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionEventPublisherPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.event.TranscriptionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Output Adapter — publica {@link TranscriptionCompletedEvent} no RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqTranscriptionEventPublisher implements TranscriptionEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(TranscriptionCompletedEvent event) {
        log.info("Publicando TranscriptionCompletedEvent | audioHash={} | fromCache={}",
                event.audioHash(), event.fromCache());

        rabbitTemplate.convertAndSend(
                RabbitMqTranscriptionConfig.TRANSCRIPTION_EXCHANGE,
                RabbitMqTranscriptionConfig.TRANSCRIPTION_ROUTING_KEY,
                event);

        log.debug("Evento publicado | exchange={} | routingKey={}",
                RabbitMqTranscriptionConfig.TRANSCRIPTION_EXCHANGE,
                RabbitMqTranscriptionConfig.TRANSCRIPTION_ROUTING_KEY);
    }
}
