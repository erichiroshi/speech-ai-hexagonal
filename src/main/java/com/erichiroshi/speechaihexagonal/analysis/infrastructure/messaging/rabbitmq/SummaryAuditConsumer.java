package com.erichiroshi.speechaihexagonal.analysis.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.analysis.domain.event.SummaryCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer de auditoria — loga eventos de resumo recebidos.
 *
 * <p>Base pronta para Fase 11: substituir log por chamada ao
 * NotificationPort (email/SMS/WhatsApp).
 */
@Slf4j
@Component
public class SummaryAuditConsumer {

    @RabbitListener(
            queues = RabbitMqAnalysisConfig.SUMMARY_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void onSummaryCompleted(SummaryCompletedEvent event) {
        log.info("[AUDIT] SummaryCompleted | audioHash={} | fromCache={} | model={} | chars={}",
                event.audioHash(),
                event.fromCache(),
                event.model(),
                event.summaryText() != null ? event.summaryText().length() : 0);
    }
}
