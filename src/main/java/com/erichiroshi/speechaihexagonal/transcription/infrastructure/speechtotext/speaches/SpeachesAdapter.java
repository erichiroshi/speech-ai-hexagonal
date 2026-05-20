package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.domain.port.out.SpeechToTextPort;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.mapper.SpeachesMapper;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.response.SpeachesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adapter de saída que implementa {@link SpeechToTextPort} via Speaches (Whisper local).
 *
 * <p>Endpoint alvo: {@code POST /v1/audio/transcriptions} (compatível OpenAI API).
 *
 * <p>Trade-off: gratuito, privado, sem latência de rede externa.
 * Requer container Speaches rodando localmente (GPU/CPU).
 * A Fase 5 adicionará {@code OpenAiSpeechAdapter} como alternativa cloud.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(SpeachesProperties.class)
public class SpeachesAdapter implements SpeechToTextPort {

    private static final String TRANSCRIPTIONS_ENDPOINT = "/v1/audio/transcriptions";

    private final RestClient restClient;
    private final SpeachesProperties properties;
    private final SpeachesMapper mapper;

    @Override
    public Transcription transcribe(byte[] audioBytes, String fileName, String contentType) {

        ByteArrayResource resource = new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return ((fileName != null && !fileName.isBlank()) ? fileName : "audio.wav");
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("model", properties.model());

        log.debug("Chamando Speaches | model={} | filename={} | bytes={}",
                properties.model(), fileName, audioBytes.length);

        try {
            SpeachesResponse speachesResponse = restClient.post()
                    .uri(TRANSCRIPTIONS_ENDPOINT)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            (_, resp) -> {
                                String errorMsg = new String(resp.getBody().readAllBytes());
                                throw new SpeechToTextException(
                                        "Speaches retornou erro %s: %s".formatted(resp.getStatusText(), errorMsg));
                            }
                    )
                    .body(SpeachesResponse.class);

            // Garantia técnica de infraestrutura caso o client retorne nulo de forma inesperada
            if (speachesResponse == null || speachesResponse.text() == null || speachesResponse.text().isBlank()) {
                throw new SpeechToTextException("Speaches retornou resposta vazia ou nula");
            }

            log.debug("Resposta Speaches | chars={}", speachesResponse.text().length());

            return mapper.toDomain(speachesResponse);

        } catch (RestClientException ex) {
            // Captura quedas de conexão, timeouts e o shutdown do MockWebServer
            log.error("Falha de comunicação com o servidor Speaches", ex);
            throw new SpeechToTextException("Servidor inacessível ou falha de rede", ex);
        }
    }
}
