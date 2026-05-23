package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.response;

import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;

import java.time.LocalDateTime;
import java.util.UUID;

public record TranscriptionResponse(
        UUID id,
        String audioHash,
        String audioTranscription,
        LocalDateTime createdAt
) {

    public static TranscriptionResponse toResponse(TranscriptionOutput output) {
        return new TranscriptionResponse(output.id(), output.audioHash(), output.text(), output.createdAt());
    }
}
