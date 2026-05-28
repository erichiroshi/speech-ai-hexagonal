package com.erichiroshi.speechaihexagonal.analysis.infrastructure.cache.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Propriedades de cache para resumos.
 *
 * <pre>
 * app:
 *   analysis:
 *     summary-ttl: 72h  # padrão
 * </pre>
 */
@ConfigurationProperties(prefix = "app.analysis.cache")
public record RedisAnalysisProperties(
        @DurationUnit(ChronoUnit.HOURS)
        Duration summaryTtl
) {
    public RedisAnalysisProperties {
        if (summaryTtl == null)
            summaryTtl = Duration.ofHours(72);
    }
}
