package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.response;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;

public record SpeachesResponse(String text) {

    public Transcription toDomain(String audioHash) {
        return new Transcription(audioHash, text);
    }
}
