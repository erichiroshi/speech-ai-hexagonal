package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.config;

import com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.SpeachesProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SpeachesProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient speachesRestClient(SpeachesProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }
}
