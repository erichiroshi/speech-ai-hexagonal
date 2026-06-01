package com.erichiroshi.speechaihexagonal.transcription.application.port.out;

import com.erichiroshi.speechaihexagonal.transcription.domain.event.TranscriptionCompletedEvent;

/**
 * Driven Port — contrato de publicação de eventos de transcrição.
 * O use case não conhece RabbitMQ ou qualquer broker de mensagens.
 */
public interface TranscriptionEventPublisherPort {

    void publish(TranscriptionCompletedEvent event);
}
