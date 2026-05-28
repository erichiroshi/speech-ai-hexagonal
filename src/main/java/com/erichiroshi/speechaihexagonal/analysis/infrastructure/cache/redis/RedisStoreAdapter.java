package com.erichiroshi.speechaihexagonal.analysis.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.analysis.application.port.out.SummaryStorePort;
import com.erichiroshi.speechaihexagonal.analysis.domain.model.Summary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Output Adapter — implementa {@link SummaryStorePort} via Redis.
 *
 * <p>Chave: {@code summary:{audioHash}}
 * <p>TTL: configurável via {@code app.analysis.summary-ttl} (padrão 72h)
 */
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisAnalysisProperties.class)
@Component
public class RedisStoreAdapter implements SummaryStorePort {

    private static final String KEY_PREFIX = "summary:";

    private final RedisAnalysisProperties properties;
    private final RedisTemplate<String, Summary> redisTemplate;

    @Override
    public Optional<Summary> findByAudioHash(String audioHash) {

        log.debug("Buscando transcrição do Redis-resumo ");

        String key = KEY_PREFIX + audioHash;
        try {
            Summary cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.debug("Cache hit (Redis-resumo) | key={}", key);
                return Optional.of(cached);
            }

            log.debug("Cache miss (Redis-resumo) | key={}", key);
            return Optional.empty();

        } catch (Exception ex) {
            log.warn("Falha ao ler cache Redis-resumo — continuando sem cache | error={}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void save(Summary summary) {
        log.debug("Salvando no cache (REDIS-resumo)");
        String key = KEY_PREFIX + summary.getAudioHash();

        try {
            redisTemplate.opsForValue().set(key, summary, properties.summaryTtl());
            log.debug("Resumo armazenado no cache (REDIS-resumo) | key={} | ttl={}h", key, properties.summaryTtl());
        } catch (Exception ex) {
            log.warn("Falha ao gravar cache Redis-resumo | error={}", ex.getMessage());
        }
    }

}
