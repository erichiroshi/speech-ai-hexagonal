package com.erichiroshi.speechaihexagonal.analysis.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.analysis.domain.event.SummaryCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SummaryAuditConsumerTest {

    private static final String HASH = "audio-abc-123";
    private SummaryAuditConsumer consumer;

    private static SummaryCompletedEvent summaryCompletedEvent(String text, String model, boolean fromCache) {
        return SummaryCompletedEvent.of(
                HASH, text, model, fromCache);
    }

    @BeforeEach
    void setUp() {
        consumer = new SummaryAuditConsumer();
    }

    @Test
    @DisplayName("Deve processar o evento de resumo e registrar o log com sucesso")
    void shouldProcessSummaryCompletedEventAndLogSuccessfully() {
        // Given
        var event = summaryCompletedEvent("Resumo completo da transcrição.", "gpt-4o", false);

        // When & Then
        assertDoesNotThrow(() -> consumer.onSummaryCompleted(event));
    }

    @Test
    @DisplayName("Deve processar o evento sem lançar exceção quando o texto do resumo for nulo")
    void shouldProcessEventSuccessfullyWhenSummaryTextIsNull() {
        // Given - Tratando a validação ternária (event.summaryText() != null)
        var event = summaryCompletedEvent(null, "llama-3", true);

        // When & Then
        assertDoesNotThrow(() -> consumer.onSummaryCompleted(event));
    }
}
