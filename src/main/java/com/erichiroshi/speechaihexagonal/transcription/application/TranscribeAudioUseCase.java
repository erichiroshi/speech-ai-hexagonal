package com.erichiroshi.speechaihexagonal.transcription.application;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.mapper.TranscriptionMapper;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.application.port.in.TranscribeAudioPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.port.out.SpeechToTextPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TranscribeAudioUseCase implements TranscribeAudioPort {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
            "audio/wav", "audio/wave", "audio/x-wav", "audio/mpeg", "audio/mp4"
    );
    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
            "wav", "mp3", "mp4", "mpeg"
    );

    private final SpeechToTextPort speechToTextPort;
    private final TranscriptionMapper mapper;

    @Override
    public TranscriptionOutput execute(TranscriptionInput input) {
        validate(input.audioBytes(), input.fileName(), input.contentType());

        log.info("Iniciando transcrição | size={}bytes", input.audioBytes().length);

        Transcription transcription = speechToTextPort.transcribe(input.audioBytes(), input.fileName(), input.contentType());

        log.info("Transcrição concluída | chars={} ", transcription.getText().length());

        return mapper.toOutput(transcription);
    }

    private void validate(byte[] audioBytes, String fileName, String contentType) {
        // Validação de presença do arquivo
        if (audioBytes == null || audioBytes.length == 0 || fileName == null || fileName.isBlank()) {
            throw new AudioValidationException("audio", "Arquivo ou nome do arquivo vazio/ausente");
        }
        // Validação de tamanho
        if (audioBytes.length > MAX_FILE_SIZE_BYTES) {
            throw new AudioValidationException("audioBytes", "Arquivo excede o limite máximo de 5 MB");
        }
        // Validação de Content-Type
        if (contentType == null || contentType.isBlank()) {
            throw new AudioValidationException("Content-Type", "Content-Type do arquivo não informado");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new AudioValidationException("Content-Type",
                    "Content-Type não suportado. Tipos aceitos: %s".formatted(ALLOWED_CONTENT_TYPES));
        }
        // Validação de Extensão (Segurança adicional)
        String extension = getFileExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new AudioValidationException("fileName",
                    "Extensão do arquivo não suportada. Extensões aceitas: %s".formatted(ALLOWED_EXTENSIONS));
        }
    }
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf + 1);
    }
}
