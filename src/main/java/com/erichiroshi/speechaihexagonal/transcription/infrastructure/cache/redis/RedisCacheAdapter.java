package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.transcription.domain.TranscriptionCachePort;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Primary
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisProperties.class)
@Component
public class RedisCacheAdapter implements TranscriptionCachePort {

    private static final String KEY_PREFIX = "transcription:";

    private final RedisProperties properties;
    private final RedisTemplate<String, Transcription> redisTemplate;

    @Override
    public Optional<Transcription> get(String audioHash) {
        log.debug("Buscando transcrição do cache");

        String key = KEY_PREFIX + audioHash;

        try {
            Transcription cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("Cache HIT | key={}", key);
            }
            return Optional.ofNullable(cached);
        } catch (Exception ex) {
            log.warn("Falha ao ler cache Redis — continuando sem cache | error={}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String audioHash, Transcription transcription) {
        log.debug("Salvando no cache");
        String key = KEY_PREFIX + audioHash;
        long ttlHours = properties.transcription().ttlHours();

        try {
            redisTemplate.opsForValue().set(key, transcription, Duration.ofHours(ttlHours));
            log.info("Cache STORE | key={} | ttl={}h", key, ttlHours);

        } catch (Exception ex) {
            log.warn("Falha ao gravar cache Redis | error={}", ex.getMessage());
        }
    }
}
