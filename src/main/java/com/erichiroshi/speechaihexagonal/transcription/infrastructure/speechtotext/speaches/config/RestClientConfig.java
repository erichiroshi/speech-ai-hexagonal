package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.config;

import com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.SpeachesProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SpeachesProperties.class)
public class RestClientConfig {

    private final SpeachesProperties properties;

    @Bean
    public RestClient speachesRestClient() {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }
}
