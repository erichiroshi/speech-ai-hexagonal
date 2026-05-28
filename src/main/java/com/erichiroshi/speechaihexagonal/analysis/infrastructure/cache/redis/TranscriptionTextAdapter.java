package com.erichiroshi.speechaihexagonal.analysis.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.analysis.application.port.out.TranscriptionTextPort;
import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionCachePort;
import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionRepositoryPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Output Adapter — implementa {@link TranscriptionTextPort}.
 *
 * <p>Ponto de integração entre os bounded contexts analysis/ e transcription/.
 * O acoplamento é deliberadamente isolado neste adapter:
 * o use case {@code SummarizeTranscriptionUseCase} nunca importa classes de transcription/.
 *
 * <p>Estratégia: Redis primeiro → PostgreSQL como fallback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TranscriptionTextAdapter implements TranscriptionTextPort {

    private final TranscriptionCachePort transcriptionCachePort;
    private final TranscriptionRepositoryPort transcriptionRepositoryPort;

    @Override
    public Optional<String> findTextByAudioHash(String audioHash) {
        // 1. Redis — camada mais rápida
        Optional<String> fromCache = transcriptionCachePort.findByAudioHash(audioHash)
                .map(Transcription::getText);

        if (fromCache.isPresent()) {
            log.debug("Texto de transcrição encontrado no Redis-transcription | audioHash={}", audioHash);
            return fromCache;
        }

        // 2. PostgreSQL — fallback
        Optional<String> fromDb = transcriptionRepositoryPort.findByAudioHash(audioHash)
                .map(Transcription::getText);

        if (fromDb.isPresent()) {
            log.debug("Texto de transcrição encontrado no PostgreSQL-transcription | audioHash={}", audioHash);
            return fromDb;
        }

        return Optional.empty();
    }
}
