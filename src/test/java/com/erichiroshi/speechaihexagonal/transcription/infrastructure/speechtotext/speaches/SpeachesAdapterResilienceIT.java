package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.config.SpeachesResilienceTestConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de integração de resiliência do SpeachesAdapter.
 *
 * <p>Contexto enxuto: apenas SpeachesAdapter + Resilience4j AOP + RestClient.
 * Sem datasource, Redis, JPA ou qualquer outro bean de infraestrutura.
 *
 * <p>WireMock simula o Speaches em diferentes cenários:
 * <ul>
 *   <li>Sucesso — resposta 200 normal</li>
 *   <li>Erro 500 repetido — abre CircuitBreaker</li>
 *   <li>CircuitBreaker OPEN — fallback imediato sem chamar Speaches</li>
 *   <li>HALF_OPEN recovery — circuito fecha após chamadas bem-sucedidas</li>
 *   <li>Retry — max-attempts=1 nos testes, deve falhar na primeira</li>
 * </ul>
 */
@SpringBootTest(
        classes = SpeachesAdapter.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Import({
        SpeachesResilienceTestConfig.class,
        AopAutoConfiguration.class,
        CircuitBreakerAutoConfiguration.class
})
@EnableConfigurationProperties
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("SpeachesAdapter — Resiliência (WireMock)")
class SpeachesAdapterResilienceIT {

    static WireMockServer wireMock = new WireMockServer(
            WireMockConfiguration.wireMockConfig().dynamicPort());

    @Autowired
    private SpeachesAdapter adapter;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeAll
    static void startWireMock() {
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.speaches.base-url", wireMock::baseUrl);
        registry.add("app.speaches.model", () -> "Systran/faster-whisper-small");
        // Janela pequena para abrir o CB rápido nos testes
        registry.add("resilience4j.circuitbreaker.instances.speaches.sliding-window-size", () -> 4);
        registry.add("resilience4j.circuitbreaker.instances.speaches.failure-rate-threshold", () -> 50);
        registry.add("resilience4j.circuitbreaker.instances.speaches.wait-duration-in-open-state", () -> "2s");
        registry.add("resilience4j.circuitbreaker.instances.speaches.permitted-number-of-calls-in-half-open-state", () -> 3);
        registry.add("resilience4j.circuitbreaker.instances.speaches.automatic-transition-from-open-to-half-open-enabled", () -> true);
        registry.add("resilience4j.circuitbreaker.instances.speaches.record-exceptions",
                () -> "org.springframework.web.client.RestClientException,java.io.IOException,java.util.concurrent.TimeoutException");
        registry.add("resilience4j.circuitbreaker.instances.speaches.record-exceptions",
                () -> "com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException");
        registry.add("resilience4j.retry.instances.speaches.max-attempts", () -> 1);
        registry.add("resilience4j.retry.instances.speaches.wait-duration", () -> "100ms");
        registry.add("resilience4j.retry.instances.speaches.ignore-exceptions",
                () -> "com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException");
        registry.add("resilience4j.bulkhead.instances.speaches.max-concurrent-calls", () -> 10);
        registry.add("resilience4j.bulkhead.instances.speaches.max-wait-duration", () -> "0ms");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private static byte[] fakeAudio() {
        return "fake-audio-bytes".getBytes();
    }

    @BeforeEach
    void reset() {
        wireMock.resetAll();
        circuitBreakerRegistry.circuitBreaker("speaches").reset();
    }

    private void stubSuccess() {
        wireMock.stubFor(post(urlPathEqualTo("/v1/audio/transcriptions"))
                .willReturn(okJson("""
                        {"text": "transcrição de teste bem-sucedida"}
                        """)));
    }

    private void stubServerError() {
        wireMock.stubFor(post(urlPathEqualTo("/v1/audio/transcriptions"))
                .willReturn(serverError().withBody("Internal Server Error")));
    }

    private void forcarCircuitoAberto() {
        stubServerError();
        for (int i = 0; i < 4; i++) {
            try {
                adapter.transcribe(fakeAudio(), "audio.wav", "audio/wav");
            } catch (SpeechToTextException _) {
                //
            }
        }
    }

    // ── Testes ───────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("deve transcrever com sucesso quando Speaches responde normalmente")
    void deveTranscreverComSucesso() {
        stubSuccess();

        Transcription result = adapter.transcribe(fakeAudio(), "audio.wav", "audio/wav");

        assertThat(result.getText()).isEqualTo("transcrição de teste bem-sucedida");
        wireMock.verify(1, postRequestedFor(urlPathEqualTo("/v1/audio/transcriptions")));
    }

    @Test
    @Order(2)
    @DisplayName("deve lançar SpeechToTextException quando Speaches retorna 500")
    void deveLancarQuandoSpeachesRetorna500() {
        stubServerError();

        // Com max-attempts=1 e SpeechToTextException no ignore-exceptions do Retry,
        // o erro original sobe diretamente (não passa pelo fallback, pois CB ainda não abriu)

        var fakeAudio = fakeAudio();
        assertThatThrownBy(() -> adapter.transcribe(fakeAudio, "audio.wav", "audio/wav"))
                .isInstanceOf(SpeechToTextException.class)
                .hasMessageContaining("Serviço de transcrição temporariamente indisponível.");
    }

    @Test
    @Order(3)
    @DisplayName("deve abrir CircuitBreaker após atingir threshold de falhas")
    void deveAbrirCircuitBreakerAposThresholdDeFalhas() {
        stubServerError();

        // 4 falhas com janela=4 e threshold=50% → CB abre
        for (int i = 0; i < 4; i++) {
            try {
                adapter.transcribe(fakeAudio(), "audio.wav", "audio/wav");
            } catch (SpeechToTextException _) {
                //
            }
        }

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("speaches");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @Order(4)
    @DisplayName("deve acionar fallback imediatamente quando CircuitBreaker está OPEN")
    void deveAcionarFallbackQuandoCircuitoAberto() {
        forcarCircuitoAberto();

        // CB OPEN — Speaches não deve ser chamado
        wireMock.resetAll();
        stubSuccess(); // stub que não deve ser atingido

        var fakeAudio = fakeAudio();
        assertThatThrownBy(() -> adapter.transcribe(fakeAudio, "audio.wav", "audio/wav"))
                .isInstanceOf(SpeechToTextException.class)
                .hasMessageContaining("temporariamente indisponível");

        wireMock.verify(0, postRequestedFor(urlPathEqualTo("/v1/audio/transcriptions")));
    }

    @Test
    @Order(5)
    @DisplayName("deve retornar ao estado CLOSED após HALF_OPEN com chamadas bem-sucedidas")
    void deveFecharCircuitoAposHalfOpen() {
        forcarCircuitoAberto();

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("speaches");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Aguarda transição automática para HALF_OPEN (wait-duration = 2s)
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .pollInterval(Duration.ofMillis(200))
                .until(() -> cb.getState() == CircuitBreaker.State.HALF_OPEN);

        stubSuccess();

        // 3 chamadas de prova bem-sucedidas em HALF_OPEN fecha o circuito
        for (int i = 0; i < 3; i++) {
            adapter.transcribe(fakeAudio(), "audio.wav", "audio/wav");
        }

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @Order(6)
    @DisplayName("deve falhar na primeira tentativa quando max-attempts=1 e erro é SpeechToTextException")
    void deveRetentarComBackoff() {
        // SpeechToTextException está em ignore-exceptions do Retry —
        // não há retentativa, falha diretamente
        wireMock.stubFor(post(urlPathEqualTo("/v1/audio/transcriptions"))
                .inScenario("retry")
                .whenScenarioStateIs("Started")
                .willReturn(serverError())
                .willSetStateTo("Segunda tentativa"));

        wireMock.stubFor(post(urlPathEqualTo("/v1/audio/transcriptions"))
                .inScenario("retry")
                .whenScenarioStateIs("Segunda tentativa")
                .willReturn(okJson("""
                        {"text": "sucedeu na segunda tentativa"}
                        """)));

        var fakeAudio = fakeAudio();
        assertThatThrownBy(() -> adapter.transcribe(fakeAudio, "audio.wav", "audio/wav"))
                .isInstanceOf(SpeechToTextException.class);

        // Speaches foi chamado apenas 1 vez (sem retentativa)
        wireMock.verify(1, postRequestedFor(urlPathEqualTo("/v1/audio/transcriptions")));
    }
}
