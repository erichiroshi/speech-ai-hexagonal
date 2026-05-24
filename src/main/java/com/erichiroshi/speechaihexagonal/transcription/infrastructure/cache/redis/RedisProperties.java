package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.cache")
public record RedisProperties(
        Duration transcriptionTtl
) {
    public RedisProperties {
        if (transcriptionTtl == null) {
            transcriptionTtl = Duration.ofHours(24);
        }
    }
}