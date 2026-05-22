package com.erichiroshi.speechaihexagonal.transcription.application;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.domain.SpeechToTextPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.TranscriptionRepository;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
    @Mock
    private SpeechToTextPort speechToTextPort;
    @Mock
    private TranscriptionRepository transcriptionRepository;
    @InjectMocks
    private TranscribeAudioUseCase useCase;

    private static Transcription fakeDomain(String text) {
        return new Transcription("a".repeat(64), text);
    }

    @Nested
    @DisplayName("deduplicação")
    class Deduplicacao {

        @Test
        @DisplayName("deve retornar transcrição existente sem chamar IA quando hash já existe")
        void deveReutilizarQuandoHashExiste() {
            Transcription existing = fakeDomain("texto reutilizado");

            when(transcriptionRepository.findByAudioHash(anyString())).thenReturn(Optional.of(existing));

            TranscriptionOutput result = useCase.execute(new TranscriptionInput(VALID_AUDIO, FILENAME, CONTENT_TYPE));

            assertThat(result.text()).isEqualTo("texto reutilizado");
            verifyNoInteractions(speechToTextPort);
            verify(transcriptionRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve chamar IA e persistir quando hash não existe")
        void deveTranscreverEPersistirQuandoHashNaoExiste() {
            Transcription fromAI = fakeDomain("texto novo da IA");
            Transcription saved = fakeDomain("texto novo da IA");

            when(transcriptionRepository.findByAudioHash(anyString())).thenReturn(Optional.empty());
            when(speechToTextPort.transcribe(any(), any(), any())).thenReturn(fromAI);
            when(transcriptionRepository.save(any())).thenReturn(saved);

            TranscriptionOutput result = useCase.execute(new TranscriptionInput(VALID_AUDIO, FILENAME, CONTENT_TYPE));

            assertThat(result.text()).isEqualTo("texto novo da IA");
            verify(speechToTextPort).transcribe(VALID_AUDIO, FILENAME, CONTENT_TYPE);
            verify(transcriptionRepository).save(any());
        }

        @Test
        @DisplayName("deve persistir com o mesmo audioHash gerado para os bytes do áudio")
        void devePersistirComHashCorreto() {
            Transcription fromAI = fakeDomain("texto");
            Transcription saved = fakeDomain("texto");

            when(transcriptionRepository.findByAudioHash(anyString())).thenReturn(Optional.empty());
            when(speechToTextPort.transcribe(any(), any(), any())).thenReturn(fromAI);
            when(transcriptionRepository.save(any())).thenReturn(saved);

            useCase.execute(new TranscriptionInput(VALID_AUDIO, FILENAME, CONTENT_TYPE));

            ArgumentCaptor<Transcription> captor = ArgumentCaptor.forClass(Transcription.class);
            verify(transcriptionRepository).save(captor.capture());

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

            verifyNoInteractions(speechToTextPort);
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