package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.application.port.in.TranscribeAudioPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.response.TranscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Adapter de entrada REST para o use case de transcrição.
 * Responsável apenas por receber a requisição HTTP e delegar ao use case via porta.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transcriptions")
public class TranscriptionController {

    private final TranscribeAudioPort transcribeAudioPort;

    @PostMapping
    public ResponseEntity<TranscriptionResponse> transcription(@RequestPart(name = "file") MultipartFile file) {

        TranscriptionInput input = toInput(file);

        TranscriptionOutput output = transcribeAudioPort.execute(input);

        return ResponseEntity.ok(TranscriptionResponse.toResponse(output));
    }

    private TranscriptionInput toInput(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AudioValidationException("file", "Nenhum arquivo foi enviado");
        }
        try {
            return new TranscriptionInput(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType());
        } catch (IOException ex) {
            throw new AudioValidationException("file", "Não foi possível ler o arquivo: " + ex.getMessage());
        }
    }
}