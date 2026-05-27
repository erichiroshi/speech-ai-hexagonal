package com.erichiroshi.speechaihexagonal.transcription.application;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.application.port.out.SpeechToTextPort;
import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionCachePort;
import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionMetricsPort;
import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionRepositoryPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.TranscriptionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TranscribeAudioUseCase")
class TranscribeAudioUseCaseTest {

    private static final byte[] VALID_AUDIO = "fake-audio-bytes".getBytes();
    private static final String FILENAME = "audio.wav";
    private static final String CONTENT_TYPE = "audio/wav";

    @Mock private SpeechToTextPort speechToTextPort;
    @Mock private TranscriptionRepositoryPort transcriptionRepositoryPort;
    @Mock private TranscriptionCachePort transcriptionCachePort;
    @Mock private TranscriptionMetricsPort metrics;

    @InjectMocks
    private TranscribeAudioUseCase useCase;

    private static Transcription fakeDomain(String text) {
        return new Transcription(new TranscriptionId(UUID.randomUUID()), "a".repeat(64), text, LocalDateTime.now());
    }

    @BeforeEach
    void setUp() {
        lenient().when(metrics.timeSpeaches(any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(0);
                    return supplier.get();
                });
    }

    @Nested
    @DisplayName("cache-aside — camadas de deduplicação")
    class CacheAside {

        @Test
        @DisplayName("deve retornar do cache Redis sem consultar banco ou IA")
        void deveRetornarDoCacheRedis() {
            Transcription cached = fakeDomain("texto do cache");

            when(transcriptionCachePort.findByAudioHash(anyString())).thenReturn(Optional.of(cached));

            TranscriptionOutput result = useCase.execute(new TranscriptionInput(VALID_AUDIO, FILENAME, CONTENT_TYPE));

            assertThat(result.text()).isEqualTo("texto do cache");
            verifyNoInteractions(transcriptionRepositoryPort);
            verifyNoInteractions(speechToTextPort);
        }

        @Test
        @DisplayName("deve consultar banco quando cache miss, popular cache e retornar")
        void devePopularCacheAoRetornarDoBanco() {
            Transcription fromDb = fakeDomain("texto do banco");

            when(transcriptionCachePort.findByAudioHash(anyString())).thenReturn(Optional.empty());
            when(transcriptionRepositoryPort.findByAudioHash(anyString())).thenReturn(Optional.of(fromDb));

            TranscriptionOutput result = useCase.execute(new TranscriptionInput(VALID_AUDIO, FILENAME, CONTENT_TYPE));

            assertThat(result.text()).isEqualTo("texto do banco");
            verify(transcriptionCachePort).save(fromDb);
            verifyNoInteractions(speechToTextPort);
        }

        @Test
        @DisplayName("deve chamar IA, persistir e popular cache quando cache e banco miss")
        void deveChamarIAEPersistirQuandoCacheEBancoMiss() {
            Transcription fromAI = fakeDomain("texto da IA");
            Transcription saved = fakeDomain("texto da IA");

            when(transcriptionCachePort.findByAudioHash(anyString())).thenReturn(Optional.empty());
            when(transcriptionRepositoryPort.findByAudioHash(anyString())).thenReturn(Optional.empty());
            when(speechToTextPort.transcribe(any(), any(), any())).thenReturn(fromAI);
            when(transcriptionRepositoryPort.save(any())).thenReturn(saved);

            TranscriptionOutput result = useCase.execute(new TranscriptionInput(VALID_AUDIO, FILENAME, CONTENT_TYPE));

            assertThat(result.text()).isEqualTo("texto da IA");
            verify(speechToTextPort).transcribe(VALID_AUDIO, FILENAME, CONTENT_TYPE);
            verify(transcriptionRepositoryPort).save(any());
            verify(transcriptionCachePort).save(saved);
        }

        @Test
        @DisplayName("deve persistir com audioHash SHA-256 de 64 chars gerado dos bytes")
        void devePersistirComHashCorreto() {
            Transcription fromAI = fakeDomain("texto");
            Transcription saved = fakeDomain("texto");

            when(transcriptionCachePort.findByAudioHash(anyString())).thenReturn(Optional.empty());
            when(transcriptionRepositoryPort.findByAudioHash(anyString())).thenReturn(Optional.empty());
            when(speechToTextPort.transcribe(any(), any(), any())).thenReturn(fromAI);
            when(transcriptionRepositoryPort.save(any())).thenReturn(saved);

            useCase.execute(new TranscriptionInput(VALID_AUDIO, FILENAME, CONTENT_TYPE));

            ArgumentCaptor<Transcription> captor = ArgumentCaptor.forClass(Transcription.class);
            verify(transcriptionRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getAudioHash()).hasSize(64).matches("[0-9a-f]+");
        }
    }

    @Nested
    @DisplayName("validação — arquivo")
    class ValidacaoArquivo {

        @Test
        @DisplayName("deve lançar AudioValidationException quando audioBytes é nulo")
        void deveLancarQuandoNulo() {
            var input = new TranscriptionInput(null, FILENAME, CONTENT_TYPE);

            assertThatThrownBy(() -> useCase.execute(input))
                    .isInstanceOf(AudioValidationException.class)
                    .hasMessageContaining("vazio");

            verifyNoInteractions(speechToTextPort, transcriptionRepositoryPort, transcriptionCachePort);
        }

        @Test
        @DisplayName("deve lançar AudioValidationException quando audioBytes está vazio")
        void deveLancarQuandoVazio() {
            var input = new TranscriptionInput(new byte[0], FILENAME, CONTENT_TYPE);

            assertThatThrownBy(() -> useCase.execute(input))
                    .isInstanceOf(AudioValidationException.class)
                    .hasMessageContaining("vazio");
        }

        @Test
        @DisplayName("deve lançar AudioValidationException quando arquivo excede 5 MB")
        void deveLancarQuandoExcede5MB() {
            byte[] grande = new byte[6 * 1024 * 1024];
            var input = new TranscriptionInput(grande, FILENAME, CONTENT_TYPE);

            assertThatThrownBy(() -> useCase.execute(input))
                    .isInstanceOf(AudioValidationException.class)
                    .hasMessageContaining("5 MB");
        }
    }

    @Nested
    @DisplayName("validação — Content-Type")
    class ValidacaoContentType {

        @Test
        @DisplayName("deve lançar AudioValidationException quando Content-Type é inválido")
        void deveLancarParaContentTypeInvalido() {
            var input = new TranscriptionInput(VALID_AUDIO, "video.mp4", "video/mp4");

            assertThatThrownBy(() -> useCase.execute(input))
                    .isInstanceOf(AudioValidationException.class)
                    .hasMessageContaining("Content-Type não suportado");
        }

        @Test
        @DisplayName("deve lançar AudioValidationException quando Content-Type é nulo")
        void deveLancarParaContentTypeNulo() {
            var input = new TranscriptionInput(VALID_AUDIO, FILENAME, null);

            assertThatThrownBy(() -> useCase.execute(input))
                    .isInstanceOf(AudioValidationException.class);
        }

        @Test
        @DisplayName("deve lançar AudioValidationException quando Content-Type é application/octet-stream")
        void deveLancarParaOctetStream() {
            var input = new TranscriptionInput(VALID_AUDIO, FILENAME, "application/octet-stream");
            assertThatThrownBy(() -> useCase.execute(input))
                    .isInstanceOf(AudioValidationException.class)
                    .hasMessageContaining("Content-Type não suportado");
        }
    }
}