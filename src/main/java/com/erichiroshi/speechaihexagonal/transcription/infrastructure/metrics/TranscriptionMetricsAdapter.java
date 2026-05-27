package com.erichiroshi.speechaihexagonal.transcription.infrastructure.metrics;

import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionMetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Centraliza todas as métricas de transcrição registradas no Micrometer.
 *
 * <p>Métricas expostas em {@code /actuator/prometheus}:
 * <ul>
 *   <li>{@code transcription.requests.total} — contador por status (success/error) e fonte (cache/db/ai)</li>
 *   <li>{@code transcription.speaches.duration} — timer com percentis p50/p95/p99</li>
 *   <li>{@code transcription.file.size.bytes} — distribution summary do tamanho do arquivo</li>
 *   <li>{@code transcription.cache.hits.total} — contador por camada (redis/db)</li>
 * </ul>
 */
@Component
public class TranscriptionMetricsAdapter implements TranscriptionMetricsPort {

    private static final String REQUESTS_TOTAL = "transcription.requests.total";
    private static final String SPEACHES_DURATION = "transcription.speaches.duration";
    private static final String FILE_SIZE = "transcription.file.size.bytes";
    private static final String CACHE_HITS = "transcription.cache.hits.total";

    private static final String TAG_STATUS = "status";
    private static final String DESC_REQUESTS_TOTAL = "Total de requisições de transcrição";

    private final Counter successCounter;
    private final Counter errorCounter;
    private final Counter cacheHitRedis;
    private final Counter cacheHitDb;
    private final Counter aiCallCounter;
    private final Timer speachesDuration;
    private final DistributionSummary fileSizeSummary;

    public TranscriptionMetricsAdapter(MeterRegistry registry) {
        this.successCounter = Counter.builder(REQUESTS_TOTAL)
                .description(DESC_REQUESTS_TOTAL)
                .tag(TAG_STATUS, "success")
                .register(registry);

        this.errorCounter = Counter.builder(REQUESTS_TOTAL)
                .description(DESC_REQUESTS_TOTAL)
                .tag(TAG_STATUS, "error")
                .register(registry);

        this.aiCallCounter = Counter.builder(REQUESTS_TOTAL)
                .description(DESC_REQUESTS_TOTAL)
                .tag(TAG_STATUS, "ai_call")
                .register(registry);

        this.cacheHitRedis = Counter.builder(CACHE_HITS)
                .description("Total de cache hits por camada")
                .tag("layer", "redis")
                .register(registry);

        this.cacheHitDb = Counter.builder(CACHE_HITS)
                .description("Total de cache hits por camada")
                .tag("layer", "db")
                .register(registry);

        this.speachesDuration = Timer.builder(SPEACHES_DURATION)
                .description("Duração das chamadas ao Speaches (speech-to-text)")
                .publishPercentiles(0.5, 0.95, 0.99)
                .publishPercentileHistogram()
                .register(registry);

        this.fileSizeSummary = DistributionSummary.builder(FILE_SIZE)
                .description("Tamanho dos arquivos de áudio enviados em bytes")
                .baseUnit("bytes")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    @Override
    public void recordSuccess() {
        successCounter.increment();
    }

    @Override
    public void recordError() {
        errorCounter.increment();
    }

    @Override
    public void recordCacheHitRedis() {
        cacheHitRedis.increment();
    }

    @Override
    public void recordCacheHitDb() {
        cacheHitDb.increment();
    }

    @Override
    public void recordAiCall() {
        aiCallCounter.increment();
    }

    @Override
    public void recordFileSize(long bytes) {
        fileSizeSummary.record(bytes);
    }

    /**
     * Executa o bloco e registra a duração automaticamente.
     * Usar para medir o tempo de chamada ao Speaches.
     */
    @Override
    public <T> T timeSpeaches(Supplier<T> supplier) {
        return speachesDuration.record(supplier);
    }
}
