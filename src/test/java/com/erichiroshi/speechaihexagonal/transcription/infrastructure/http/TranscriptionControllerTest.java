package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http;

import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.application.port.in.TranscribeAudioPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.handler.GlobalExceptionHandler;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.mapper.TranscriptionHttpMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TranscriptionController.class)
@Import({GlobalExceptionHandler.class, TranscriptionHttpMapperImpl.class})
@DisplayName("TranscriptionController")
class TranscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TranscribeAudioPort transcribeAudio;

    private static final byte[] AUDIO_BYTES = "fake-audio".getBytes();

    @Nested
    @DisplayName("POST /api/transcriptions — caminho feliz")
    class CaminhoFeliz {

        @Test
        @DisplayName("deve retornar 200 com audioTranscription quando arquivo é válido")
        void deveRetornar200QuandoValido() throws Exception {
            when(transcribeAudio.execute(any()))
                    .thenReturn(new TranscriptionOutput("Olá, este é o texto transcrito."));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "audio.wav", "audio/wav", AUDIO_BYTES);

            mockMvc.perform(multipart("/api/transcriptions").file(file))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.audioTranscription", is("Olá, este é o texto transcrito.")));
        }

        @Test
        @DisplayName("deve retornar 200 para arquivo audio/mpeg")
        void deveRetornar200ParaMp3() throws Exception {
            when(transcribeAudio.execute(any()))
                    .thenReturn(new TranscriptionOutput("Texto do MP3"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "audio.mp3", "audio/mpeg", AUDIO_BYTES);

            mockMvc.perform(multipart("/api/transcriptions").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.audioTranscription", is("Texto do MP3")));
        }
    }

    @Nested
    @DisplayName("POST /api/transcriptions — erros 4xx")
    class Erros4xx {

        @Test
        @DisplayName("deve retornar 400 com ProblemDetail quando Content-Type é inválido")
        void deveRetornar400ParaContentTypeInvalido() throws Exception {
            when(transcribeAudio.execute(any()))
                    .thenThrow(new AudioValidationException("file",
                            "Content-Type não suportado: 'video/mp4'"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "video.mp4", "video/mp4", AUDIO_BYTES);

            mockMvc.perform(multipart("/api/transcriptions").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Arquivo de áudio inválido")))
                    .andExpect(jsonPath("$.field", is("file")))
                    .andExpect(jsonPath("$.detail").exists());
        }

        @Test
        @DisplayName("deve retornar 400 quando nenhum arquivo é enviado")
        void deveRetornar400QuandoSemArquivo() throws Exception {
            when(transcribeAudio.execute(any()))
                    .thenThrow(new AudioValidationException("file",
                            "Arquivo vazio ou ausente"));

            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "", MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[0]);

            mockMvc.perform(multipart("/api/transcriptions").file(emptyFile))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("deve retornar 400 quando arquivo excede 5 MB")
        void deveRetornar400QuandoArquivoGrande() throws Exception {
            when(transcribeAudio.execute(any()))
                    .thenThrow(new AudioValidationException("file",
                            "Arquivo excede o tamanho máximo de 5 MB"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "grande.wav", "audio/wav", AUDIO_BYTES);

            mockMvc.perform(multipart("/api/transcriptions").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Arquivo de áudio inválido")));
        }
    }

    @Nested
    @DisplayName("POST /api/transcriptions — erros 5xx")
    class Erros5xx {

        @Test
        @DisplayName("deve retornar 502 quando Speaches falha")
        void deveRetornar502QuandoSpeachesFalha() throws Exception {
            when(transcribeAudio.execute(any()))
                    .thenThrow(new SpeechToTextException("Speaches retornou erro 500: Internal Server Error"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "audio.wav", "audio/wav", AUDIO_BYTES);

            mockMvc.perform(multipart("/api/transcriptions").file(file))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.title", is("Falha no motor de transcrição")));
        }

        @Test
        @DisplayName("deve retornar 500 para exceção genérica inesperada")
        void deveRetornar500ParaExcecaoGenerica() throws Exception {
            when(transcribeAudio.execute(any()))
                    .thenThrow(new RuntimeException("Erro inesperado"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "audio.wav", "audio/wav", AUDIO_BYTES);

            mockMvc.perform(multipart("/api/transcriptions").file(file))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.title", is("Erro interno")));
        }
    }
}