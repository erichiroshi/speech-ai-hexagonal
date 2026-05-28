package com.erichiroshi.speechaihexagonal.analysis.infrastructure.cache.redis;

import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionCachePort;
import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionRepositoryPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription.newTranscription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TranscriptionTextAdapter")
class TranscriptionTextAdapterTest {

    @Mock
    private TranscriptionCachePort transcriptionCachePort;

    @Mock
    private TranscriptionRepositoryPort transcriptionRepositoryPort;

    private TranscriptionTextAdapter adapter;

    private static final String AUDIO_HASH = "a1b2c3d4e5f6g7h8i9j0";
    private static final String TEXT_TRANSCRIPTION = "Texto transcrito com sucesso.";

    @BeforeEach
    void setUp() {
        adapter = new TranscriptionTextAdapter(transcriptionCachePort, transcriptionRepositoryPort);
    }

    private Transcription fakeTranscription() {
        return newTranscription(AUDIO_HASH, TEXT_TRANSCRIPTION);
    }

    @Nested
    @DisplayName("caminho feliz (com cache hit)")
    class CacheHit {

        @Test
        @DisplayName("deve retornar o texto do Redis e não consultar o PostgreSQL")
        void deveRetornarTextoDoRedis() {
            // Arrange
            Transcription transcription = fakeTranscription();
            when(transcriptionCachePort.findByAudioHash(AUDIO_HASH))
                    .thenReturn(Optional.of(transcription));

            // Act
            Optional<String> result = adapter.findTextByAudioHash(AUDIO_HASH);

            // Assert
            assertThat(result).contains(TEXT_TRANSCRIPTION);
            verify(transcriptionCachePort, times(1)).findByAudioHash(AUDIO_HASH);
            verifyNoInteractions(transcriptionRepositoryPort);
        }
    }

    @Nested
    @DisplayName("fallback (cache miss)")
    class CacheMiss {

        @Test
        @DisplayName("deve buscar e retornar o texto do PostgreSQL quando não estiver no Redis")
        void deveRetornarTextoDoPostgresql() {
            // Arrange
            Transcription transcription = fakeTranscription();
            when(transcriptionCachePort.findByAudioHash(AUDIO_HASH))
                    .thenReturn(Optional.empty());
            when(transcriptionRepositoryPort.findByAudioHash(AUDIO_HASH))
                    .thenReturn(Optional.of(transcription));

            // Act
            Optional<String> result = adapter.findTextByAudioHash(AUDIO_HASH);

            // Assert
            assertThat(result).contains(TEXT_TRANSCRIPTION);
            verify(transcriptionCachePort, times(1)).findByAudioHash(AUDIO_HASH);
            verify(transcriptionRepositoryPort, times(1)).findByAudioHash(AUDIO_HASH);
        }

        @Test
        @DisplayName("deve retornar Optional.empty quando não encontrar o registro em nenhuma das bases")
        void deveRetornarEmptyQuandoRegistroNaoExistir() {
            // Arrange
            when(transcriptionCachePort.findByAudioHash(AUDIO_HASH))
                    .thenReturn(Optional.empty());
            when(transcriptionRepositoryPort.findByAudioHash(AUDIO_HASH))
                    .thenReturn(Optional.empty());

            // Act
            Optional<String> result = adapter.findTextByAudioHash(AUDIO_HASH);

            // Assert
            assertThat(result).isEmpty();
            verify(transcriptionCachePort, times(1)).findByAudioHash(AUDIO_HASH);
            verify(transcriptionRepositoryPort, times(1)).findByAudioHash(AUDIO_HASH);
        }
    }
}
