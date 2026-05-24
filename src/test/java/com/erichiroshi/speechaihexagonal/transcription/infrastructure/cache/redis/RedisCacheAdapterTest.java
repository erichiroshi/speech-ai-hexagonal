package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription.newTranscription;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

    private final String audioHash = "abc123hash";
    private final String expectedKey = "transcription:abc123hash";

    @Mock private RedisProperties properties;
    @Mock private RedisTemplate<String, Transcription> redisTemplate;
    @Mock private ValueOperations<String, Transcription> valueOperations;

    @InjectMocks
    private RedisCacheAdapter cacheAdapter;

    private Transcription transcriptionMock;

    @BeforeEach
    void setUp() {
        // Configura o mock do RedisTemplate para sempre retornar as operações de valor
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        transcriptionMock = newTranscription(audioHash, "transcription text"); // Defina propriedades se necessário
    }

    @Nested
    @DisplayName("Testes do método GET")
    class GetTests {

        @Test
        @DisplayName("Deve retornar Optional com a transcrição quando houver Cache HIT")
        void shouldReturnTranscriptionOnCacheHit() {
            when(valueOperations.get(expectedKey)).thenReturn(transcriptionMock);

            Optional<Transcription> result = cacheAdapter.findByAudioHash(audioHash);

            assertTrue(result.isPresent());
            assertEquals(transcriptionMock, result.get());
            verify(valueOperations, times(1)).get(expectedKey);
        }

        @Test
        @DisplayName("Deve retornar Optional vazio quando houver Cache MISS")
        void shouldReturnEmptyOptionalOnCacheMiss() {
            when(valueOperations.get(expectedKey)).thenReturn(null);

            Optional<Transcription> result = cacheAdapter.findByAudioHash(audioHash);

            assertTrue(result.isEmpty());
            verify(valueOperations, times(1)).get(expectedKey);
        }

        @Test
        @DisplayName("Deve capturar exceção e retornar Optional vazio se o Redis falhar")
        void shouldReturnEmptyOptionalWhenRedisFailsOnGet() {
            when(valueOperations.get(expectedKey)).thenThrow(new RuntimeException("Redis indisponível"));

            Optional<Transcription> result = cacheAdapter.findByAudioHash(audioHash);

            assertTrue(result.isEmpty());
            verify(valueOperations, times(1)).get(expectedKey);
        }
    }

    @Nested
    @DisplayName("Testes do método PUT")
    class PutTests {

        @Test
        @DisplayName("Deve salvar o registro com o TTL correto configurado")
        void shouldSaveRecordWithConfiguredTtl() {
            Duration expectedTtl = Duration.ofHours(5);
            when(properties.transcriptionTtl()).thenReturn(expectedTtl);

            cacheAdapter.save(transcriptionMock);

            verify(valueOperations, times(1)).set(expectedKey, transcriptionMock, expectedTtl);
        }

        @Test
        @DisplayName("Deve silenciar a exceção e logar o erro se o Redis falhar no salvamento")
        void shouldHandleExceptionWhenRedisFailsOnPut() {
            Duration expectedTtl = Duration.ofHours(2);
            when(properties.transcriptionTtl()).thenReturn(expectedTtl);

            doThrow(new RuntimeException("Erro de conexão")).when(valueOperations)
                    .set(expectedKey, transcriptionMock, expectedTtl);

            // Não deve lançar exceção para o chamador
            assertDoesNotThrow(() -> cacheAdapter.save(transcriptionMock));
            verify(valueOperations, times(1)).set(expectedKey, transcriptionMock, expectedTtl);
        }
    }
}
