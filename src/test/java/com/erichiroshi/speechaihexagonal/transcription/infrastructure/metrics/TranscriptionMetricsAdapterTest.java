package com.erichiroshi.speechaihexagonal.transcription.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TranscriptionMetrics")
class TranscriptionMetricsAdapterTest {

    private MeterRegistry registry;
    private TranscriptionMetricsAdapter metrics;

    @BeforeEach
    void setUp() {
        // Usa um registro em memória real do Micrometer, sem precisar de Mocks!
        registry = new SimpleMeterRegistry();
        metrics = new TranscriptionMetricsAdapter(registry);
    }

    @Test
    @DisplayName("deve incrementar o contador de sucesso com as tags corretas")
    void deveIncrementarSucesso() {
        metrics.recordSuccess();

        double count = registry.get("transcription.requests.total")
                .tag("status", "success")
                .counter()
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("deve incrementar o cache hit do redis com a tag correta")
    void deveIncrementarCacheRedis() {
        metrics.recordCacheHitRedis();

        double count = registry.get("transcription.cache.hits.total")
                .tag("layer", "redis")
                .counter()
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("deve registrar o tamanho do arquivo no summary")
    void deveRegistrarTamanhoArquivo() {
        metrics.recordFileSize(5000L);

        double totalAmount = registry.get("transcription.file.size.bytes")
                .summary()
                .totalAmount();

        assertThat(totalAmount).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("deve medir o tempo de execução da lambda e retornar o valor correto")
    void deveMedirTempoExecucao() {
        String resultado = metrics.timeSpeaches(() -> "texto_transcrito");

        assertThat(resultado).isEqualTo("texto_transcrito");

        double totalTime = registry.get("transcription.speaches.duration")
                .timer()
                .totalTime(java.util.concurrent.TimeUnit.NANOSECONDS);

        assertThat(totalTime).isPositive();
    }

    @Test
    @DisplayName("deve repassar a exceção original e registrar no timer caso a lambda falhe")
    void deveRepassarExcecaoSeFalhar() {
        Supplier<String> falha = () -> { throw new IllegalArgumentException("Erro da IA"); };

        assertThatThrownBy(() -> metrics.timeSpeaches(falha))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Erro da IA");
    }
}
