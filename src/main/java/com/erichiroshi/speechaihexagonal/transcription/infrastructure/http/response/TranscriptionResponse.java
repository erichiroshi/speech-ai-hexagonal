package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.response;

import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;

public record TranscriptionResponse(String audioHash, String audioTranscription) {

    public static TranscriptionResponse toResponse(TranscriptionOutput output) {
        return new TranscriptionResponse(output.audioHash(), output.text());
    }
}
