package com.erichiroshi.speechaihexagonal.transcription.domain.model;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;

public class Transcription {
    private final String text;

    public Transcription(String text) {
        if (text == null || text.isBlank()) {
            throw new SpeechToTextException("A transcrição retornada está vazia");
        }
        this.text = text;
    }

    public String getText() {
        return text;
    }

}
