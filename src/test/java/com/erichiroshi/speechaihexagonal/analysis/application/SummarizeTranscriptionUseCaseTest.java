package com.erichiroshi.speechaihexagonal.analysis.application;

import com.erichiroshi.speechaihexagonal.analysis.application.output.SummaryOutput;
import com.erichiroshi.speechaihexagonal.analysis.application.port.out.LanguageModelPort;
import com.erichiroshi.speechaihexagonal.analysis.application.port.out.SummaryStorePort;
import com.erichiroshi.speechaihexagonal.analysis.application.port.out.TranscriptionTextPort;
import com.erichiroshi.speechaihexagonal.analysis.domain.exception.AnalysisUnavailableException;
import com.erichiroshi.speechaihexagonal.analysis.domain.exception.TranscriptionNotFoundException;
import com.erichiroshi.speechaihexagonal.analysis.domain.model.Summary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SummarizeTranscriptionUseCase")
class SummarizeTranscriptionUseCaseTest {

    @Mock private TranscriptionTextPort transcriptionTextPort;
    @Mock private LanguageModelPort     languageModelPort;
    @Mock private SummaryStorePort      summaryStorePort;

    @InjectMocks
    private SummarizeTranscriptionUseCase useCase;

    private static final String AUDIO_HASH = "a".repeat(64);
    private static final String TEXT       = "Texto longo da transcrição de áudio para teste.";
    private static final String SUMMARY    = "Resumo conciso do texto transcrito.";

    private static Summary fakeSummary() {
        return Summary.newSummary(AUDIO_HASH, SUMMARY, "qwen2.5:0.5b");
    }

    @Nested
    @DisplayName("cache hit — resumo já existe no Redis")
    class CacheHit {

        @Test
        @DisplayName("deve retornar resumo existente sem chamar LLM")
        void deveRetornarResumoCacheado() {
            when(summaryStorePort.findByAudioHash(AUDIO_HASH))
                    .thenReturn(Optional.of(fakeSummary()));

            SummaryOutput result = useCase.execute(AUDIO_HASH);

            assertThat(result.cached()).isTrue();
            assertThat(result.summary()).isEqualTo(SUMMARY);
            verifyNoInteractions(transcriptionTextPort, languageModelPort);
        }
    }

    @Nested
    @DisplayName("cache miss — geração via LLM")
    class CacheMiss {

        @Test
        @DisplayName("deve buscar texto, chamar LLM, salvar e retornar com cached=false")
        void deveGerarResumoPelaIA() {
            when(summaryStorePort.findByAudioHash(AUDIO_HASH)).thenReturn(Optional.empty());
            when(transcriptionTextPort.findTextByAudioHash(AUDIO_HASH)).thenReturn(Optional.of(TEXT));
            when(languageModelPort.generate(anyString())).thenReturn(SUMMARY);

            SummaryOutput result = useCase.execute(AUDIO_HASH);

            assertThat(result.cached()).isFalse();
            assertThat(result.summary()).isEqualTo(SUMMARY);
            assertThat(result.audioHash()).isEqualTo(AUDIO_HASH);
            verify(summaryStorePort).save(any(Summary.class));
        }

        @Test
        @DisplayName("deve lançar TranscriptionNotFoundException quando texto não existe")
        void deveLancarQuandoTranscricaoNaoExiste() {
            when(summaryStorePort.findByAudioHash(AUDIO_HASH)).thenReturn(Optional.empty());
            when(transcriptionTextPort.findTextByAudioHash(AUDIO_HASH)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(AUDIO_HASH))
                    .isInstanceOf(TranscriptionNotFoundException.class)
                    .hasMessageContaining(AUDIO_HASH);

            verifyNoInteractions(languageModelPort);
            verify(summaryStorePort, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar AnalysisUnavailableException quando LLM falha")
        void deveLancarQuandoLLMFalha() {
            when(summaryStorePort.findByAudioHash(AUDIO_HASH)).thenReturn(Optional.empty());
            when(transcriptionTextPort.findTextByAudioHash(AUDIO_HASH)).thenReturn(Optional.of(TEXT));
            when(languageModelPort.generate(anyString()))
                    .thenThrow(new RuntimeException("Ollama indisponível"));

            assertThatThrownBy(() -> useCase.execute(AUDIO_HASH))
                    .isInstanceOf(AnalysisUnavailableException.class)
                    .hasMessageContaining("Ollama indisponível");

            verify(summaryStorePort, never()).save(any());
        }
    }
}
