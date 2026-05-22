package com.erichiroshi.speechaihexagonal.transcription.application;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.application.port.in.TranscribeAudioPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.SpeechToTextPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.TranscriptionRepository;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.domain.service.AudioHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TranscribeAudioUseCase implements TranscribeAudioPort {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
            "audio/wav", "audio/wave", "audio/mpeg", "audio/mp3", "audio/mp4", "audio/webm", "audio/ogg");

    private final SpeechToTextPort speechToTextPort;
    private final TranscriptionRepository transcriptionRepository;

    @Override
    public TranscriptionOutput execute(TranscriptionInput input) {
        validate(input);

        String audioHash = AudioHashService.generate(input.audioBytes());

        // 1. Deduplicação - reutiliza se já existe
        log.debug("Consultando transcrição existente | audioHash={}", audioHash);
        Optional<Transcription> existing = transcriptionRepository.findByAudioHash(audioHash);
        if (existing.isPresent()) {
            log.info("Transcription existente | audioHash={}", audioHash);
            return TranscriptionOutput.toOutput(existing.get());
        }

        // 2. Transcrever via IA
        log.info("Iniciando transcrição | filename={} | size={}bytes", input.fileName(), input.audioBytes().length);

        Transcription transcribed = speechToTextPort.transcribe(
                input.audioBytes(), input.fileName(), input.contentType());

        log.info("Transcrição concluída | filename={} | chars={}", input.fileName(), transcribed.getText().length());

        // 3. Persistir
        Transcription saved = transcriptionRepository.save(transcribed);

        log.info("Transcrição persistida | audioHash: {}", saved.getAudioHash());

        return TranscriptionOutput.toOutput(saved);
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
