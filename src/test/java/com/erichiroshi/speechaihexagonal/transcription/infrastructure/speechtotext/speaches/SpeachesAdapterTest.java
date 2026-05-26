package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários do SpeachesAdapter com MockWebServer.
 * Sem Spring context — valida apenas o comportamento HTTP do adapter.
 * Resiliência (CB/Retry/Bulkhead) é testada em SpeachesAdapterResilienceIT.
 */
@DisplayName("SpeachesAdapter — unitário")
class SpeachesAdapterTest {

    private static MockWebServer mockWebServer;
    private SpeachesAdapter adapter;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        String baseUrl = mockWebServer.url("/").toString();
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        SpeachesProperties properties = new SpeachesProperties(baseUrl, "Systran/faster-whisper-small");
        adapter = new SpeachesAdapter(restClient, properties);
    }

    @Nested
    @DisplayName("caminho feliz")
    class CaminhoFeliz {

        @Test
        @DisplayName("deve retornar Transcription com texto da resposta Speaches")
        void deveRetornarTranscription() throws InterruptedException {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("""
                            {"text": "Olá, isto é um teste de transcrição."}
                            """));

            Transcription result = adapter.transcribe("audio".getBytes(), "audio.wav", "audio/wav");

            assertThat(result.getText()).isEqualTo("Olá, isto é um teste de transcrição.");

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/v1/audio/transcriptions");
            assertThat(request.getHeader("Content-Type")).contains("multipart/form-data");
        }

        @Test
        @DisplayName("deve usar 'audio.wav' como nome de arquivo padrão quando fileName é nulo")
        void deveUsarNomePadraoQuandoNulo() throws InterruptedException {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("""
                            {"text": "transcrição sem nome"}
                            """));

            Transcription result = adapter.transcribe("audio".getBytes(), null, "audio/wav");

            assertThat(result.getText()).isEqualTo("transcrição sem nome");

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getBody().readUtf8()).contains("audio.wav");
        }
    }

    @Nested
    @DisplayName("erros HTTP")
    class ErrosHttp {

        @Test
        @DisplayName("deve lançar SpeechToTextException quando Speaches retorna 500")
        void deveLancarQuando500() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"error\": \"Internal Server Error\"}"));

            var audioBytes = "audio".getBytes();
            assertThatThrownBy(() -> adapter.transcribe(audioBytes, "audio.wav", "audio/wav"))
                    .isInstanceOf(SpeechToTextException.class)
                    .hasMessageContaining("Speaches retornou erro");
        }

        @Test
        @DisplayName("deve lançar SpeechToTextException quando Speaches retorna 422")
        void deveLancarQuando422() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(422)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"error\": \"Unprocessable Entity\"}"));

            var audioBytes = "audio".getBytes();
            assertThatThrownBy(() -> adapter.transcribe(audioBytes, "audio.wav", "audio/wav"))
                    .isInstanceOf(SpeechToTextException.class);
        }

        @Test
        @DisplayName("deve lançar SpeechToTextException quando resposta está vazia")
        void deveLancarQuandoRespostaVazia() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"text\": \"\"}"));

            var audioBytes = "audio".getBytes();
            assertThatThrownBy(() -> adapter.transcribe(audioBytes, "audio.wav", "audio/wav"))
                    .isInstanceOf(SpeechToTextException.class);
        }
    }

    @Nested
    @DisplayName("falha de rede")
    class FalhaDeRede {

        @Test
        @DisplayName("deve lançar SpeechToTextException quando servidor fecha conexão")
        void deveLancarQuandoServidorFechaConexao() {
            mockWebServer.enqueue(new MockResponse().setSocketPolicy(
                    okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST));

            var audioBytes = "audio".getBytes();
            assertThatThrownBy(() -> adapter.transcribe(audioBytes, "audio.wav", "audio/wav"))
                    .isInstanceOf(SpeechToTextException.class)
                    .hasMessageContaining("inacessível ou falha de rede");
        }
    }
}