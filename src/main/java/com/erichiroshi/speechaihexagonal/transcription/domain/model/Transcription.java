package com.erichiroshi.speechaihexagonal.transcription.domain.model;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade de domínio que representa uma transcrição persistida.
 * Contém UUID, hash SHA-256 do áudio, conteúdo transcrito e timestamp de criação.
 * Java puro — sem anotações de framework.
 */
public class Transcription {

    private final UUID id;
    private final String audioHash;
    private final String text;
    private final Instant createdAt;

    public Transcription(String text) {
        this.id = null;
        this.audioHash = null;
        this.text = text;
        this.createdAt = null;
        newTranscription(null, text);
    }

    /**
     * Construtor para restauração a partir da persistência (todos os campos já existem).
     */
    public Transcription(UUID id, String audioHash, String text, Instant createdAt) {
        if (text == null || text.isBlank()) {
            throw new SpeechToTextException("A transcrição retornada está vazia");
        }
        this.id = id;
        this.audioHash = audioHash;
        this.text = text;
        this.createdAt = createdAt;
    }

    /**
     * Factory method para criação de nova transcrição (antes de persistir).
     * Gera UUID e timestamp automaticamente.
     */
    public static Transcription newTranscription(String audioHash, String text) {
        return new Transcription(UUID.randomUUID(), audioHash, text, Instant.now());
    }

    public UUID getId() {
        return id;
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
