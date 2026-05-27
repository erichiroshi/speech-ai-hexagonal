package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches;

import com.erichiroshi.speechaihexagonal.transcription.application.port.out.SpeechToTextPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.response.SpeachesResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
 * <p>Camadas de resiliência (ordem de execução: Bulkhead → CircuitBreaker → Retry → método):
 * <ul>
 *   <li>{@code @Bulkhead} — limita chamadas simultâneas (max 10). Rejeita imediatamente
 *       se todas as threads estiverem ocupadas, evitando cascata de lentidão.</li>
 *   <li>{@code @CircuitBreaker} — abre após 50% de falhas em janela de 10 chamadas.
 *       OPEN aguarda 30s → HALF_OPEN (3 provas) → CLOSED.</li>
 *   <li>{@code @Retry} — até 3 tentativas com backoff exponencial (1s → 2s → 4s).
 *       Apenas para {@code ResourceAccessException} e {@code IOException}.
 *       {@link SpeechToTextException} é ignorada (não faz sentido retentar erro 4xx).</li>
 *   <li>Fallback (CircuitBreaker): lança {@link SpeechToTextException} com mensagem clara.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(SpeachesProperties.class)
public class SpeachesAdapter implements SpeechToTextPort {

    private static final String INSTANCE_NAME = "speaches";
    private static final String TRANSCRIPTIONS_ENDPOINT = "/v1/audio/transcriptions";

    private final RestClient restClient;
    private final SpeachesProperties properties;

    @Override
    @Bulkhead(name = INSTANCE_NAME)
    @CircuitBreaker(name = INSTANCE_NAME, fallbackMethod = "fallback")
    @Retry(name = INSTANCE_NAME)
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

            if (speachesResponse == null || speachesResponse.text() == null || speachesResponse.text().isBlank()) {
                throw new SpeechToTextException("Speaches retornou resposta vazia ou nula");
            }

            log.debug("Resposta Speaches | chars={}", speachesResponse.text().length());

            return speachesResponse.toDomain();

        } catch (RestClientException ex) {
            log.error("Falha de comunicação com o servidor Speaches", ex);
            throw new SpeechToTextException("Servidor inacessível ou falha de rede", ex);
        }
    }

    /**
     * Fallback acionado pelo CircuitBreaker quando o circuito está OPEN
     * ou quando a chamada falha e esgota o Retry.
     */
    Transcription fallback(Throwable cause) {
        if (cause instanceof CallNotPermittedException)
            log.warn("CircuitBreaker [{}] ativado (OPEN) | Chamada rejeitada automaticamente", INSTANCE_NAME);
        else
            log.warn("CircuitBreaker [{}] ativado (CLOSE) | motivo={}", INSTANCE_NAME, cause.getMessage());

        throw new SpeechToTextException(
                "Serviço de transcrição temporariamente indisponível. Tente novamente em instantes.", cause);
    }
}
