package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.config;

import com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.SpeachesProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

/**
 * Configuração mínima para testes de resiliência do SpeachesAdapter.
 * Registra apenas o RestClient e as propriedades necessárias —
 * sem datasource, Redis, JPA ou qualquer outro bean de infraestrutura.
 */
@TestConfiguration
@EnableConfigurationProperties(SpeachesProperties.class)
public class SpeachesResilienceTestConfig {

    @Bean
    public RestClient speachesRestClient(SpeachesProperties properties) {
        // 1. Cria o HttpClient nativo travado em HTTP/1.1
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        // 2. Encapsula na fábrica do Spring
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        // 3. Monta o RestClient utilizando a fábrica ajustada
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}