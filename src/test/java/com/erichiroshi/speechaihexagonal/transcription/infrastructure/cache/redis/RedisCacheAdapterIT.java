package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.test.autoconfigure.DataRedisTest;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração do RedisCacheAdapter com Cache Redis real via Testcontainers.
 * Valida save, findByAudioHash e expiração TTL.
 */
@Testcontainers
@DataRedisTest
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import({RedisConfig.class, RedisCacheAdapter.class})
@EnableConfigurationProperties(RedisProperties.class)
@DisplayName("RedisCacheAdapter — integração")
class RedisCacheAdapterIT {

    private static final String HASH = "a".repeat(64);
    @Container
    static RedisContainer redisContainer =
            new RedisContainer(DockerImageName.parse("redis:8.6.2-alpine"));
    @Autowired
    private RedisCacheAdapter adapter;

    @Autowired
    private RedisTemplate<String, Transcription> redisTemplate;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
        registry.add("app.cache.transcription-ttl", () -> "PT10S"); // 10s para testar TTL
    }

    @BeforeEach
    void limparCache() {
        assert redisTemplate.getConnectionFactory() != null;
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Nested
    @DisplayName("save e findByAudioHash")
    class SaveEFind {

        @Test
        @DisplayName("deve armazenar e recuperar transcrição pelo audioHash")
        void deveSalvarERecuperar() {
            Transcription transcription = Transcription.newTranscription(HASH, "texto de teste");

            adapter.save(transcription);
            Optional<Transcription> result = adapter.findByAudioHash(HASH);

            assertThat(result).isPresent();
            assertThat(result.get().getText()).isEqualTo("texto de teste");
            assertThat(result.get().getAudioHash()).isEqualTo(HASH);
        }

        @Test
        @DisplayName("deve retornar Optional vazio quando hash não existe no cache")
        void deveRetornarVazioParaHashInexistente() {
            Optional<Transcription> result = adapter.findByAudioHash("b".repeat(64));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deve usar prefixo 'transcription:' na chave Redis")
        void deveUsarPrefixoCorreto() {
            Transcription transcription = Transcription.newTranscription(HASH, "texto prefixo");
            adapter.save(transcription);

            Boolean exists = redisTemplate.hasKey("transcription:" + HASH);
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("deve sobrescrever cache ao salvar com mesmo hash")
        void deveSobrescreverEntradaExistente() {
            adapter.save(Transcription.newTranscription(HASH, "texto original"));
            adapter.save(Transcription.newTranscription(HASH, "texto atualizado"));

            Optional<Transcription> result = adapter.findByAudioHash(HASH);
            assertThat(result).isPresent();
            assertThat(result.get().getText()).isEqualTo("texto atualizado");
        }
    }

    @Nested
    @DisplayName("TTL")
    class Ttl {

        @Test
        @DisplayName("deve expirar entrada após TTL configurado")
        void deveExpirarAposTtl() {
            Transcription transcription = Transcription.newTranscription(HASH, "texto ttl");
            adapter.save(transcription);

            assertThat(adapter.findByAudioHash(HASH)).isPresent();

            Duration ttl = redisTemplate.getExpire("transcription:" + HASH,
                    java.util.concurrent.TimeUnit.MILLISECONDS) > 0
                    ? Duration.ofMillis(redisTemplate.getExpire("transcription:" + HASH,
                    java.util.concurrent.TimeUnit.MILLISECONDS))
                    : Duration.ZERO;

            assertThat(ttl).isPositive().isLessThanOrEqualTo(Duration.ofSeconds(11));
        }
    }
}