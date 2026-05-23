package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "speaches")
public record SpeachesProperties(
        String baseUrl,
        String model
) {
}