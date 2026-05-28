package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.openai;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAiSpeechAdapter")
class OpenAiSpeechAdapterTest {

    @Mock
    private OpenAiAudioTranscriptionModel transcriptionModel;

    private OpenAiSpeechAdapter adapter;

    private static final byte[] AUDIO_BYTES = "fake-audio-bytes".getBytes();
    private static final String FILENAME    = "audio.wav";
    private static final String CONTENT_TYPE = "audio/wav";

    @BeforeEach
    void setUp() {
        OpenAiSpeechProperties properties = new OpenAiSpeechProperties("whisper-1");
        adapter = new OpenAiSpeechAdapter(transcriptionModel, properties);
    }

    private AudioTranscriptionResponse fakeResponse(String text) {
        AudioTranscription transcription = new AudioTranscription(text);
        return new AudioTranscriptionResponse(transcription);
    }

    @Nested
    @DisplayName("caminho feliz")
    class CaminhoFeliz {

        @Test
        @DisplayName("deve retornar Transcription com texto da resposta OpenAI")
        void deveRetornarTranscription() {
            when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                    .thenReturn(fakeResponse("Olá, transcrição via OpenAI."));

            Transcription result = adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE);

            assertThat(result.getText()).isEqualTo("Olá, transcrição via OpenAI.");
        }

        @Test
        @DisplayName("deve usar 'audio.wav' como nome padrão quando fileName é nulo")
        void deveUsarNomePadraoQuandoNulo() {
            when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                    .thenReturn(fakeResponse("texto sem nome"));

            Transcription result = adapter.transcribe(AUDIO_BYTES, null, CONTENT_TYPE);

            assertThat(result.getText()).isEqualTo("texto sem nome");
        }
    }

    @Nested
    @DisplayName("erros")
    class Erros {

        @Test
        @DisplayName("deve lançar SpeechToTextException quando OpenAI retorna texto vazio")
        void deveLancarQuandoTextoVazio() {
            when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                    .thenReturn(fakeResponse(""));

            assertThatThrownBy(() -> adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE))
                    .isInstanceOf(SpeechToTextException.class)
                    .hasMessageContaining("vazia");
        }

        @Test
        @DisplayName("deve lançar SpeechToTextException quando OpenAI lança exceção genérica")
        void deveLancarQuandoOpenAIFalha() {
            when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                    .thenThrow(new RuntimeException("API key inválida"));

            assertThatThrownBy(() -> adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE))
                    .isInstanceOf(SpeechToTextException.class)
                    .hasMessageContaining("Falha ao transcrever via OpenAI");
        }

        @Test
        @DisplayName("deve propagar SpeechToTextException sem envolver em nova exceção")
        void devePropagarSpeechToTextException() {
            when(transcriptionModel.call(any(AudioTranscriptionPrompt.class)))
                    .thenReturn(fakeResponse("  "));

            assertThatThrownBy(() -> adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE))
                    .isInstanceOf(SpeechToTextException.class)
                    .hasMessageContaining("vazia");
        }
    }
}
