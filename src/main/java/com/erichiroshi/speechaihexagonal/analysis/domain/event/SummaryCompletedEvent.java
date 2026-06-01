package com.erichiroshi.speechaihexagonal.analysis.domain.event;

import java.time.Instant;

/**
 * Evento de domínio publicado após um resumo ser gerado com sucesso.
 *
 * <p>Carrega dados necessários para notificações da fase seguinte.
 */
public record SummaryCompletedEvent(
        String  audioHash,
        String  summaryText,
        String  model,
        boolean fromCache,
        Instant occurredAt
) {
    public static SummaryCompletedEvent of(
            String audioHash, String summaryText, String model, boolean fromCache) {
        return new SummaryCompletedEvent(audioHash, summaryText, model, fromCache, Instant.now());
    }
}
