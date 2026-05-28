package com.erichiroshi.speechaihexagonal.analysis.infrastructure.llm.ollama;

import com.erichiroshi.speechaihexagonal.analysis.domain.exception.AnalysisUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OllamaLanguageModelAdapter")
class OllamaLanguageModelAdapterTest {

    // O parâmetro Answers.RETURNS_DEEP_STUBS permite mockar a chamada fluente: .prompt().user().call().content()
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private OllamaLanguageModelAdapter adapter;

    private static final String PROMPT = "Resuma o seguinte texto...";

    @BeforeEach
    void setUp() {
        OllamaProperties properties = new OllamaProperties("qwen2.5:0.5b", "http://ollama:11434");
        adapter = new OllamaLanguageModelAdapter(chatClient, properties);
    }

    @Nested
    @DisplayName("caminho feliz")
    class CaminhoFeliz {

        @Test
        @DisplayName("deve retornar a resposta do Ollama limpa (trim) com sucesso")
        void deveRetornarRespostaComSucesso() {
            String respostaEsperada = "Este é o resumo gerado.";

            when(chatClient.prompt().user(anyString()).call().content())
                    .thenReturn("   " + respostaEsperada + "   ");

            String result = adapter.generate(PROMPT);

            assertThat(result).isEqualTo(respostaEsperada);
        }
    }

    @Nested
    @DisplayName("erros")
    class Erros {

        @Test
        @DisplayName("deve lançar AnalysisUnavailableException quando Ollama retorna resposta nula")
        void deveLancarQuandoRespostaNula() {
            when(chatClient.prompt().user(anyString()).call().content())
                    .thenReturn(null);

            assertThatThrownBy(() -> adapter.generate(PROMPT))
                    .isInstanceOf(AnalysisUnavailableException.class)
                    .hasMessageContaining("Ollama retornou resposta vazia");
        }

        @Test
        @DisplayName("deve lançar AnalysisUnavailableException quando Ollama retorna texto em branco")
        void deveLancarQuandoRespostaEmBranco() {
            when(chatClient.prompt().user(anyString()).call().content())
                    .thenReturn("    ");

            assertThatThrownBy(() -> adapter.generate(PROMPT))
                    .isInstanceOf(AnalysisUnavailableException.class)
                    .hasMessageContaining("Ollama retornou resposta vazia");
        }

        @Test
        @DisplayName("deve lançar AnalysisUnavailableException quando ocorre exceção genérica no cliente")
        void deveLancarQuandoClienteFalha() {
            when(chatClient.prompt().user(anyString()).call())
                    .thenThrow(new RuntimeException("Connection refused"));

            assertThatThrownBy(() -> adapter.generate(PROMPT))
                    .isInstanceOf(AnalysisUnavailableException.class)
                    .hasMessageContaining("Ollama indisponível")
                    .hasMessageContaining("Connection refused");
        }

        @Test
        @DisplayName("deve propagar AnalysisUnavailableException sem encapsular em nova exceção")
        void devePropagarAnalysisUnavailableException() {
            // Provoca a condição que dispara a própria AnalysisUnavailableException de dentro do bloco try
            when(chatClient.prompt().user(anyString()).call().content())
                    .thenReturn("");

            assertThatThrownBy(() -> adapter.generate(PROMPT))
                    .isInstanceOf(AnalysisUnavailableException.class)
                    .hasMessageContaining("resposta vazia")
                    // Garante que não foi empacotada com a mensagem "Ollama indisponível:" do bloco catch genérico
                    .hasMessageNotContaining("Ollama indisponível:");
        }
    }
}
