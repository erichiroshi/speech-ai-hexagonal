package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionCachePort;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Primary
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisProperties.class)
@Component
public class RedisCacheAdapter implements TranscriptionCachePort {

    private static final String KEY_PREFIX = "transcription:";

    private final RedisProperties redisProperties;
    private final RedisTemplate<String, Transcription> redisTemplate;

    @Override
    public Optional<Transcription> findByAudioHash(String audioHash) {
        log.debug("Buscando transcrição do cache");

        String key = KEY_PREFIX + audioHash;

        try {
            Transcription cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("Cache HIT (Redis) | key={}", key);
                return Optional.of(cached);
            }

            log.debug("Cache miss (Redis) | key={}", key);
            return Optional.empty();

        } catch (Exception ex) {
            log.warn("Falha ao ler cache Redis — continuando sem cache | error={}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void save(Transcription transcription) {
        log.debug("Salvando no cache (REDIS)");
        String key = KEY_PREFIX + transcription.getAudioHash();

        try {
            redisTemplate.opsForValue().set(key, transcription, redisProperties.transcriptionTtl());
            log.info("Cache STORE | key={} | ttl={}h", key, redisProperties.transcriptionTtl());

        } catch (Exception ex) {
            log.warn("Falha ao gravar cache Redis | error={}", ex.getMessage());
        }
    }
}
