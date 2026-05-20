package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.mapper.SpeachesMapperImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SpeachesAdapter")
class SpeachesAdapterTest {

    private MockWebServer mockWebServer;
    private SpeachesAdapter adapter;

    private static final byte[] AUDIO_BYTES = "fake-audio-content".getBytes();
    private static final String FILENAME   = "audio.wav";
    private static final String CONTENT_TYPE = "audio/wav";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        SpeachesProperties properties = new SpeachesProperties(
                mockWebServer.url("/").toString(),
                "Systran/faster-whisper-small"
        );

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();

        adapter = new SpeachesAdapter(restClient, properties, new SpeachesMapperImpl());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("caminho feliz")
    class CaminhoFeliz {

        @Test
        @DisplayName("deve retornar Transcription quando Speaches responde com texto")
        void deveRetornarTranscriptionComSucesso() throws InterruptedException {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"text\": \"Olá, este é o texto transcrito.\"}"));

            Transcription result = adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE);

            assertThat(result).isNotNull();
            assertThat(result.getText()).isEqualTo("Olá, este é o texto transcrito.");

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/v1/audio/transcriptions");
            assertThat(request.getHeader("Content-Type")).contains("multipart/form-data");
        }

        @Test
        @DisplayName("deve usar filename original no multipart quando fornecido")
        void deveUsarFilenameOriginalNoMultipart() throws InterruptedException {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"text\": \"texto\"}"));

            adapter.transcribe(AUDIO_BYTES, "meu-audio.mp3", "audio/mpeg");

            RecordedRequest request = mockWebServer.takeRequest();
            String bodyString = request.getBody().readUtf8();
            assertThat(bodyString).contains("meu-audio.mp3");
        }

        @Test
        @DisplayName("deve usar audio.wav como filename padrão quando filename é nulo")
        void deveUsarFilenamePadraoQuandoNulo() throws InterruptedException {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"text\": \"texto\"}"));

            adapter.transcribe(AUDIO_BYTES, null, CONTENT_TYPE);

            RecordedRequest request = mockWebServer.takeRequest();
            String bodyString = request.getBody().readUtf8();
            assertThat(bodyString).contains("audio.wav");
        }

        @Test
        @DisplayName("deve incluir o model correto no body do request")
        void deveIncluirModelNoBody() throws InterruptedException {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"text\": \"texto\"}"));

            adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE);

            RecordedRequest request = mockWebServer.takeRequest();
            String bodyString = request.getBody().readUtf8();
            assertThat(bodyString).contains("Systran/faster-whisper-small");
        }
    }

    @Nested
    @DisplayName("erros do Speaches")
    class ErrosSpeaches {

        @Test
        @DisplayName("deve lançar SpeechToTextException quando Speaches retorna 500")
        void deveLancarQuandoStatus500() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setBody("Internal Server Error"));

            assertThatThrownBy(() -> adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE))
                    .isInstanceOf(SpeechToTextException.class);
        }

        @Test
        @DisplayName("deve lançar SpeechToTextException quando Speaches retorna 422")
        void deveLancarQuandoStatus422() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(422)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"detail\": \"Unprocessable entity\"}"));

            assertThatThrownBy(() -> adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE))
                    .isInstanceOf(SpeechToTextException.class);
        }

        @Test
        @DisplayName("deve lançar SpeechToTextException quando resposta tem texto vazio")
        void deveLancarQuandoTextoVazio() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"text\": \"\"}"));

            assertThatThrownBy(() -> adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE))
                    .isInstanceOf(SpeechToTextException.class)
                    .hasMessageContaining("vazia");
        }

        @Test
        @DisplayName("deve lançar SpeechToTextException quando resposta tem texto apenas com espaços")
        void deveLancarQuandoTextoApenasBrancos() {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody("{\"text\": \"   \"}"));

            assertThatThrownBy(() -> adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE))
                    .isInstanceOf(SpeechToTextException.class);
        }

        @Test
        @DisplayName("deve lançar SpeechToTextException quando servidor está inacessível")
        void deveLancarQuandoServidorInacessivel() throws IOException {
            mockWebServer.shutdown();

            assertThatThrownBy(() -> adapter.transcribe(AUDIO_BYTES, FILENAME, CONTENT_TYPE))
                    .isInstanceOf(SpeechToTextException.class);
        }
    }
}