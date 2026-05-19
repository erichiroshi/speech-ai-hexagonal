package com.erichiroshi.speechaihexagonal.transcription.application;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.mapper.TranscriptionMapper;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.domain.port.out.SpeechToTextPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TranscribeAudioUseCase")
class TranscribeAudioUseCaseTest {

    @Mock
    private SpeechToTextPort speechToTextPort;

    @Mock
    private TranscriptionMapper mapper;

    @InjectMocks
    private TranscribeAudioUseCase useCase;

    private static final byte[] VALID_AUDIO = "fake-audio-bytes".getBytes();
    private static final String FILENAME = "audio.wav";
    private static final String CONTENT_TYPE = "audio/wav";

    @Nested
    @DisplayName("caminho feliz")
    class CaminhoFeliz {

        @Test
        @DisplayName("deve transcrever e retornar output quando input é válido")
        void deveTranscreveERetornarOutput() {
            Transcription domain = new Transcription("Olá mundo");
            TranscriptionOutput expected = new TranscriptionOutput("Olá mundo");

            when(speechToTextPort.transcribe(VALID_AUDIO, FILENAME, CONTENT_TYPE)).thenReturn(domain);
            when(mapper.toOutput(domain)).thenReturn(expected);

            TranscriptionOutput result = useCase.execute(new TranscriptionInput(VALID_AUDIO, FILENAME, CONTENT_TYPE));

            assertThat(result.text()).isEqualTo("Olá mundo");
            verify(speechToTextPort).transcribe(VALID_AUDIO, FILENAME, CONTENT_TYPE);
            verify(mapper).toOutput(domain);
        }

        @Test
        @DisplayName("deve aceitar audio/mpeg como Content-Type válido")
        void deveAceitarAudioMpeg() {
            Transcription domain = new Transcription("texto mp3");
            TranscriptionOutput expected = new TranscriptionOutput("texto mp3");

            when(speechToTextPort.transcribe(any(), any(), eq("audio/mpeg"))).thenReturn(domain);
            when(mapper.toOutput(domain)).thenReturn(expected);

            TranscriptionOutput result = useCase.execute(
                    new TranscriptionInput(VALID_AUDIO, "audio.mp3", "audio/mpeg"));

            assertThat(result.text()).isEqualTo("texto mp3");
        }

        @Test
        @DisplayName("deve aceitar audio/mp4 como Content-Type válido")
        void deveAceitarAudioMp4() {
            Transcription domain = new Transcription("texto mp4");
            when(speechToTextPort.transcribe(any(), any(), eq("audio/mp4"))).thenReturn(domain);
            when(mapper.toOutput(domain)).thenReturn(new TranscriptionOutput("texto mp4"));

            TranscriptionOutput result = useCase.execute(
                    new TranscriptionInput(VALID_AUDIO, "audio.mp4", "audio/mp4"));

            assertThat(result.text()).isEqualTo("texto mp4");
        }
    }

    @Nested
    @DisplayName("validação — arquivo")
    class ValidacaoArquivo {

        @Test
        @DisplayName("deve lançar AudioValidationException quando audioBytes é nulo")
        void deveLancarQuandoNulo() {
            assertThatThrownBy(() ->
                    useCase.execute(new TranscriptionInput(null, FILENAME, CONTENT_TYPE)))
                    .isInstanceOf(AudioValidationException.class)
                    .hasMessageContaining("vazio");

            verifyNoInteractions(speechToTextPort);
        }

        @Test
        @DisplayName("deve lançar AudioValidationException quando audioBytes está vazio")
        void deveLancarQuandoVazio() {
            assertThatThrownBy(() ->
                    useCase.execute(new TranscriptionInput(new byte[0], FILENAME, CONTENT_TYPE)))
                    .isInstanceOf(AudioValidationException.class)
                    .hasMessageContaining("vazio");
        }

        @Test
        @DisplayName("deve lançar AudioValidationException quando arquivo excede 5 MB")
        void deveLancarQuandoExcede5MB() {
            byte[] grande = new byte[6 * 1024 * 1024];

            assertThatThrownBy(() ->
                    useCase.execute(new TranscriptionInput(grande, FILENAME, CONTENT_TYPE)))
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
            assertThatThrownBy(() ->
                    useCase.execute(new TranscriptionInput(VALID_AUDIO, "video.mp4", "video/mp4")))
                    .isInstanceOf(AudioValidationException.class)
                    .hasMessageContaining("Content-Type não suportado");
        }

        @Test
        @DisplayName("deve lançar AudioValidationException quando Content-Type é nulo")
        void deveLancarParaContentTypeNulo() {
            assertThatThrownBy(() ->
                    useCase.execute(new TranscriptionInput(VALID_AUDIO, FILENAME, null)))
                    .isInstanceOf(AudioValidationException.class);
        }

        @Test
        @DisplayName("deve lançar AudioValidationException quando Content-Type é application/octet-stream")
        void deveLancarParaOctetStream() {
            assertThatThrownBy(() ->
                    useCase.execute(new TranscriptionInput(VALID_AUDIO, FILENAME, "application/octet-stream")))
                    .isInstanceOf(AudioValidationException.class)
                    .hasMessageContaining("Content-Type não suportado");
        }
    }
}