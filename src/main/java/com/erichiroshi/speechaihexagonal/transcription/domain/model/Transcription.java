package com.erichiroshi.speechaihexagonal.transcription.domain.model;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;

import java.time.Instant;

/**
 * Entidade de domínio que representa uma transcrição persistida.
 * Contém UUID, hash SHA-256 do áudio, conteúdo transcrito e timestamp de criação.
 * Java puro — sem anotações de framework.
 */
public class Transcription {

    private final String audioHash;
    private final String text;
    private final Instant createdAt;

    public Transcription(String text) {
        this.audioHash = null;
        this.text = text;
        this.createdAt = null;
        newTranscription(null, text);
    }

    /**
     * Construtor para restauração a partir da persistência (todos os campos já existem).
     */
    public Transcription(String audioHash, String text, Instant createdAt) {
        if (text == null || text.isBlank()) {
            throw new SpeechToTextException("A transcrição retornada está vazia");
        }
        this.audioHash = audioHash;
        this.text = text;
        this.createdAt = createdAt;
    }

    /**
     * Factory method para criação de nova transcrição (antes de persistir).
     * Gera UUID e timestamp automaticamente.
     */
    public static Transcription newTranscription(String audioHash, String text) {
        return new Transcription(audioHash, text, Instant.now());
    }

    public String getAudioHash() {
        return audioHash;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
