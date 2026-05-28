package com.erichiroshi.speechaihexagonal.analysis.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.analysis.domain.model.Summary;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataRedisTest
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import({RedisAnalysisConfig.class, RedisStoreAdapter.class})
@EnableConfigurationProperties(RedisAnalysisProperties.class)
@DisplayName("RedisStoreAdapter — integração")
class RedisStoreAdapterIT {

    @Container
    static RedisContainer redisContainer =
            new RedisContainer(DockerImageName.parse("redis:8.6.2-alpine"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
        registry.add("app.analysis.summary-ttl", () -> "PT1H");
        registry.add("app.analysis.model", () -> "qwen2.5:0.5b");
        registry.add("app.analysis.base-url", () -> "http://localhost:11434");
    }

    @Autowired
    private RedisStoreAdapter adapter;

    @Autowired
//    @Qualifier("summaryRedisTemplate")
    private RedisTemplate<String, Summary> redisTemplate;

    private static final String HASH = "b".repeat(64);

    @BeforeEach
    void limparCache() {
        assert redisTemplate.getConnectionFactory() != null;
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Nested
    @DisplayName("save e findByAudioHash")
    class SaveEFind {

        @Test
        @DisplayName("deve armazenar e recuperar resumo pelo audioHash")
        void deveSalvarERecuperar() {
            Summary summary = Summary.newSummary(HASH, "Resumo do áudio.", "qwen2.5:0.5b");

            adapter.save(summary);
            Optional<Summary> result = adapter.findByAudioHash(HASH);

            assertThat(result).isPresent();
            assertThat(result.get().getText()).isEqualTo("Resumo do áudio.");
            assertThat(result.get().getAudioHash()).isEqualTo(HASH);
            assertThat(result.get().getModel()).isEqualTo("qwen2.5:0.5b");
        }

        @Test
        @DisplayName("deve retornar Optional vazio quando hash não existe")
        void deveRetornarVazioParaHashInexistente() {
            Optional<Summary> result = adapter.findByAudioHash("c".repeat(64));
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deve usar prefixo 'summary:' na chave Redis")
        void deveUsarPrefixoCorreto() {
            Summary summary = Summary.newSummary(HASH, "texto", "qwen2.5:0.5b");
            adapter.save(summary);

            assertThat(redisTemplate.hasKey("summary:" + HASH)).isTrue();
        }
    }
}
