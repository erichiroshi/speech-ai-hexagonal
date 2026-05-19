package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.application.port.in.TranscribeAudioPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.mapper.TranscriptionHttpMapper;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.response.TranscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transcriptions")
public class TranscriptionController {

    private final TranscribeAudioPort transcribeAudioPort;
    private final TranscriptionHttpMapper mapper;

    @PostMapping
    public ResponseEntity<TranscriptionResponse> transcription(@RequestPart(name = "file") MultipartFile file) {

        TranscriptionOutput output = transcribeAudioPort.execute(toTranscriptionInput(file));

        return ResponseEntity.ok(mapper.toResponse(output));
    }

    private TranscriptionInput toTranscriptionInput(MultipartFile file) {
        byte[] audioBytes = extractBytes(file);
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        return new TranscriptionInput(audioBytes, fileName, contentType);
    }

    private byte[] extractBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new AudioValidationException("file", "Não foi possível ler o arquivo: " + ex.getMessage());
        }
    }
}