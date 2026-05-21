package com.erichiroshi.speechaihexagonal.transcription.application;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.mapper.TranscriptionMapper;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.application.port.in.TranscribeAudioPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.domain.port.out.SpeechToTextPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case de transcrição de áudio — implementa a porta de entrada {@link TranscribeAudioPort}.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Validar o arquivo de áudio (tamanho e Content-Type)</li>
 *   <li>Delegar a transcrição para a porta de saída {@link SpeechToTextPort}</li>
 *   <li>Mapear o resultado de domínio para o output da aplicação</li>
 * </ul>
 *
 * <p>Não depende de nenhum adapter concreto — apenas interfaces.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TranscribeAudioUseCase implements TranscribeAudioPort {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
            "audio/wav", "audio/wave", "audio/mpeg", "audio/mp3", "audio/mp4", "audio/webm", "audio/ogg");

    private final SpeechToTextPort speechToTextPort;
    private final TranscriptionMapper mapper;

    @Override
    public TranscriptionOutput execute(TranscriptionInput input) {
        validate(input);

        log.info("Iniciando transcrição | filename={} | size={}bytes", input.fileName(), input.audioBytes().length);

        Transcription transcription = speechToTextPort.transcribe(
                input.audioBytes(),
                input.fileName(),
                input.contentType());

        log.info("Transcrição concluída | filename={} | chars={}", input.fileName(), transcription.getText().length());

        return mapper.toOutput(transcription);
    }

    private void validate(TranscriptionInput input) {
        // Validação de presença do arquivo
        if (input.audioBytes() == null || input.audioBytes().length == 0 || input.fileName() == null || input.fileName().isBlank()) {
            throw new AudioValidationException("file", "Arquivo ou nome do arquivo vazio/ausente");
        }
        // Validação de tamanho
        if (input.audioBytes().length > MAX_FILE_SIZE_BYTES) {
            throw new AudioValidationException("file", "Arquivo excede o tamanho máximo de 5 MB (recebido: %d bytes)"
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
