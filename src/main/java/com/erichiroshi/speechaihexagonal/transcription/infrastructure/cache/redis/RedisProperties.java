package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
public record RedisProperties(
        TranscriptionProperties transcription
) {
    public record TranscriptionProperties(
            Integer ttlHours
    ) {
    }
}