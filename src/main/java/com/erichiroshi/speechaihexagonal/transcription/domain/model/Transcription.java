package com.erichiroshi.speechaihexagonal.transcription.domain.model;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;

public class Transcription {

    private final String audioHash;
    private final String text;

    public Transcription(String audioHash, String text) {
        if (text == null || text.isBlank()) {
            throw new SpeechToTextException("A transcrição retornada está vazia");
        }
        this.audioHash = audioHash;
        this.text = text;
    }

    public static Transcription newTranscription(String audioHash, String text) {
        return new Transcription(audioHash, text);
    }

    public String getAudioHash() {
        return audioHash;
    }

    public String getText() {
        return text;
    }

}
