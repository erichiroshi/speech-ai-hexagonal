package com.erichiroshi.speechaihexagonal.analysis.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Summary {

    private final UUID id;
    private final String audioHash;
    private final String text;
    private final String model;
    private final Instant createdAt;

    public Summary(UUID id, String audioHash, String text, String model, Instant createdAt) {
        this.id = id;
        this.audioHash = audioHash;
        this.text = text;
        this.model = model;
        this.createdAt = createdAt;
    }

    public static Summary newSummary(String audioHash, String text, String model) {
        return new Summary(UUID.randomUUID(), audioHash, text, model, Instant.now());
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

    public String getModel() {
        return model;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
