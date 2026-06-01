package com.erichiroshi.speechaihexagonal.transcription.domain.event;

import java.time.Instant;

/**
 * Evento de domínio publicado após uma transcrição bem-sucedida.
 *
 * <p>Carrega os dados necessários para notificações futuras (email, SMS, WhatsApp)
 * e para auditoria de eventos.
 */
public record TranscriptionCompletedEvent(
        String  audioHash,
        String  text,
        String  fileName,
        long    fileSizeBytes,
        boolean fromCache,
        Instant occurredAt
) {
    public static TranscriptionCompletedEvent of(
            String audioHash, String text, String fileName, long fileSizeBytes, boolean fromCache) {
        return new TranscriptionCompletedEvent(audioHash, text, fileName, fileSizeBytes, fromCache, Instant.now());
    }
}
