package com.erichiroshi.speechaihexagonal.analysis.application.port.out;

import com.erichiroshi.speechaihexagonal.analysis.domain.event.SummaryCompletedEvent;

/**
 * Driven Port — contrato de publicação de eventos de resumo.
 * O use case não conhece RabbitMQ ou qualquer broker de mensagens.
 */
public interface SummaryEventPublisherPort {

    void publish(SummaryCompletedEvent event);
}
