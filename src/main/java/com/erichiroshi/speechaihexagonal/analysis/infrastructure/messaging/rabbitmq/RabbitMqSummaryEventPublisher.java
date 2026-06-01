package com.erichiroshi.speechaihexagonal.analysis.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.analysis.application.port.out.SummaryEventPublisherPort;
import com.erichiroshi.speechaihexagonal.analysis.domain.event.SummaryCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Output Adapter — publica {@link SummaryCompletedEvent} no RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqSummaryEventPublisher implements SummaryEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(SummaryCompletedEvent event) {
        log.info("Publicando SummaryCompletedEvent | audioHash={} | fromCache={}",
                event.audioHash(), event.fromCache());

        rabbitTemplate.convertAndSend(
                RabbitMqAnalysisConfig.SUMMARY_EXCHANGE,
                RabbitMqAnalysisConfig.SUMMARY_ROUTING_KEY,
                event);

        log.debug("Evento publicado | exchange={} | routingKey={}",
                RabbitMqAnalysisConfig.SUMMARY_EXCHANGE,
                RabbitMqAnalysisConfig.SUMMARY_ROUTING_KEY);
    }
}
