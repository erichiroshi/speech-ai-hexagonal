package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.response;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;

import static com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription.newTranscription;

public record SpeachesResponse(String text) {

    public Transcription toDomain() {
        return newTranscription("", this.text);
    }
}
