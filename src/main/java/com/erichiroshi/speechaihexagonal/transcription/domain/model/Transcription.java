package com.erichiroshi.speechaihexagonal.transcription.domain.model;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;

import java.time.LocalDateTime;

public class Transcription {

    private final TranscriptionId id;
    private final String audioHash;
    private final String text;
    private final LocalDateTime createdAt;

    public Transcription(TranscriptionId id, String audioHash, String text, LocalDateTime createdAt) {
        if (text == null || text.isBlank()) {
            throw new SpeechToTextException("A transcrição retornada está vazia");
        }
        this.id = id;
        this.audioHash = audioHash;
        this.text = text;
        this.createdAt = createdAt;
    }

    public static Transcription newTranscription(String audioHash, String text) {
        return new Transcription(new TranscriptionId(), audioHash, text, LocalDateTime.now());
    }

    public TranscriptionId getId() {
        return id;
    }

    public String getAudioHash() {
        return audioHash;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
