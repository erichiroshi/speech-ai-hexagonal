package com.erichiroshi.speechaihexagonal.transcription.application;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.application.port.in.TranscribeAudioPort;
import com.erichiroshi.speechaihexagonal.transcription.application.port.out.*;
import com.erichiroshi.speechaihexagonal.transcription.domain.event.TranscriptionCompletedEvent;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.domain.service.AudioHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TranscribeAudioUseCase implements TranscribeAudioPort {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
            "audio/wav", "audio/wave", "audio/mpeg", "audio/mp3", "audio/mp4", "audio/webm", "audio/ogg");

    private final SpeechToTextPort speechToTextPort;
    private final TranscriptionRepositoryPort transcriptionRepositoryPort;
    private final TranscriptionCachePort transcriptionCachePort;
    private final TranscriptionEventPublisherPort eventPublisherPort;
    private final TranscriptionMetricsPort metrics;

    @Transactional
    @Override
    public TranscriptionOutput execute(TranscriptionInput input) {
        validate(input);
        metrics.recordFileSize(input.audioBytes().length);

        String audioHash = AudioHashService.generate(input.audioBytes());
        try {
            // 1. verifica cache
            Optional<Transcription> fromCache = transcriptionCachePort.findByAudioHash(audioHash);
            if (fromCache.isPresent()) {
                log.info("Cache hit | audioHash={}", audioHash);
                metrics.recordCacheHitRedis();
                metrics.recordSuccess();
                publishTranscriptionEvent(fromCache.get(), input, true);
                return TranscriptionOutput.toOutput(fromCache.get());
            }

            // 2. verifica banco de dados
            Optional<Transcription> fromDb = transcriptionRepositoryPort.findByAudioHash(audioHash);
            if (fromDb.isPresent()) {
                log.info("Cache hit (DB) | audioHash={} — populando Cache", audioHash);
                metrics.recordCacheHitDb();
                metrics.recordSuccess();
                transcriptionCachePort.save(fromDb.get());
                publishTranscriptionEvent(fromDb.get(), input, true);
                return TranscriptionOutput.toOutput(fromDb.get());
            }

            // 3. Transcrever via IA
            log.info("Cache miss total — transcrevendo | filename={} | size={}bytes",
                    input.fileName(), input.audioBytes().length);
            metrics.recordAiCall();

            Transcription transcribed = metrics.timeSpeaches(
                    () -> speechToTextPort.transcribe(input.audioBytes(), input.fileName(), input.contentType()));

            // 4. Persistir no postgres e no cache
            Transcription toSave = Transcription.newTranscription(audioHash, transcribed.getText());
            Transcription saved = transcriptionRepositoryPort.save(toSave);
            transcriptionCachePort.save(saved);

            log.info("Transcrição concluída e persistida | audioHash: {}", saved.getAudioHash());
            metrics.recordSuccess();
            publishTranscriptionEvent(saved, input, false);

            return TranscriptionOutput.toOutput(saved);

        } catch (Exception ex) {
            metrics.recordError();
            throw ex;
        }
    }

    private void publishTranscriptionEvent(Transcription transcription, TranscriptionInput input, boolean fromCache) {
        try {
            eventPublisherPort.publish(TranscriptionCompletedEvent.of(
                    transcription.getAudioHash(),
                    transcription.getText(),
                    input.fileName(),
                    input.audioBytes().length,
                    fromCache));
        } catch (Exception ex) {
            // Falha no publisher não deve impedir a resposta ao cliente
            log.warn("Falha ao publicar TranscriptionCompletedEvent | audioHash={}", transcription.getAudioHash(), ex);
        }
    }

    private void validate(TranscriptionInput input) {
        // Validação de presença do arquivo
        if (input.audioBytes() == null || input.audioBytes().length == 0
                || input.fileName() == null || input.fileName().isBlank()) {
            throw new AudioValidationException("file", "Arquivo ou nome do arquivo vazio/ausente");
        }
        // Validação de tamanho
        if (input.audioBytes().length > MAX_FILE_SIZE_BYTES) {
            throw new AudioValidationException("file",
                    "Arquivo excede o tamanho máximo de 5 MB (recebido: %d bytes)"
                            .formatted(input.audioBytes().length));
        }
        // Validação de Content-Type
        if (input.contentType() == null || input.contentType().isBlank()) {
            throw new AudioValidationException("Content-Type", "Content-Type do arquivo não informado");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(input.contentType().toLowerCase())) {
            throw new AudioValidationException("Content-Type",
                    "Content-Type não suportado. Tipos aceitos: %s".formatted(ALLOWED_CONTENT_TYPES));
        }
    }
}
