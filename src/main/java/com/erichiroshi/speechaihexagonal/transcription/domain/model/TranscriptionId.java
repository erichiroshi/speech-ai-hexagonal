package com.erichiroshi.speechaihexagonal.transcription.domain.model;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;

import java.util.UUID;

public record TranscriptionId(UUID id) {

    public TranscriptionId() {
        this(UUID.randomUUID());
    }

    public TranscriptionId(UUID id) {
        if (id == null ) {
            throw new SpeechToTextException("TranscriptionId está nulo");
        }
        this.id = id;
    }
}
