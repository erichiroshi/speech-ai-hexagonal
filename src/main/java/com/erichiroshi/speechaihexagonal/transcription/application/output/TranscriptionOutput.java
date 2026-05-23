package com.erichiroshi.speechaihexagonal.transcription.application.output;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription.newTranscription;

public record TranscriptionOutput(
        UUID id,
        String audioHash,
        String text,
        LocalDateTime createdAt) {

    public static TranscriptionOutput toOutput(Transcription transcription) {
        return new TranscriptionOutput(
                transcription.getId().id(),
                transcription.getAudioHash(),
                transcription.getText(),
                transcription.getCreatedAt());
    }

    public Transcription toDomain() {
        return newTranscription(audioHash, text);
    }
}
