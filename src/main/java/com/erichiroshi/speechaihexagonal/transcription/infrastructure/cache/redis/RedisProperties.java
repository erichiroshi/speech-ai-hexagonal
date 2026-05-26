package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ConfigurationProperties(prefix = "app.cache")
public record RedisProperties(
        @DurationUnit(ChronoUnit.HOURS)
        Duration transcriptionTtl
) {
    public RedisProperties {
        if (transcriptionTtl == null) {
            transcriptionTtl = Duration.ofHours(24);
        }
    }
}