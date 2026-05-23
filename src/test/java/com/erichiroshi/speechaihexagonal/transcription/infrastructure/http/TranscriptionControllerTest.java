package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http;

import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.application.port.in.TranscribeAudioPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TranscriptionController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("TranscriptionController")
class TranscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TranscribeAudioPort transcribeAudioPort;

    private static final byte[] AUDIO_BYTES = "fake-audio".getBytes();
    private static final UUID FIXED_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final LocalDateTime FIXED_TIME = LocalDateTime.parse("2025-01-01T10:00:00");
    private static final String AUDIO_HASH = "a".repeat(64);

    private static TranscriptionOutput fakeOutput(String text) {
        return new TranscriptionOutput(FIXED_ID, AUDIO_HASH, text, FIXED_TIME);
    }

    @Nested
    @DisplayName("POST /api/transcriptions — caminho feliz")
    class CaminhoFeliz {

        @Test
        @DisplayName("deve retornar 200 com transcrição nova")
        void deveRetornar200ComTranscricaoNova() throws Exception {
            when(transcribeAudioPort.execute(any())).thenReturn(fakeOutput("Olá, texto transcrito."));

            mockMvc.perform(multipart("/api/transcriptions")
                            .file(new MockMultipartFile("file", "audio.wav", "audio/wav", AUDIO_BYTES)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.audioTranscription", is("Olá, texto transcrito.")));
        }

        @Test
        @DisplayName("deve retornar 200 com transcrição reutilizada (dedup transparente ao controller)")
        void deveRetornar200ComTranscricaoReutilizada() throws Exception {
            when(transcribeAudioPort.execute(any())).thenReturn(fakeOutput("Texto reutilizado"));

            mockMvc.perform(multipart("/api/transcriptions")
                            .file(new MockMultipartFile("file", "audio.wav", "audio/wav", AUDIO_BYTES)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.audioTranscription", is("Texto reutilizado")));
        }

        @Test
        @DisplayName("deve aceitar audio/mpeg")
        void deveAceitarMpeg() throws Exception {
            when(transcribeAudioPort.execute(any())).thenReturn(fakeOutput("Texto mp3"));

            mockMvc.perform(multipart("/api/transcriptions")
                            .file(new MockMultipartFile("file", "audio.mp3", "audio/mpeg", AUDIO_BYTES)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.audioTranscription", is("Texto mp3")));
        }
    }

    @Nested
    @DisplayName("POST /api/transcriptions — erros 4xx")
    class Erros4xx {

        @Test
        @DisplayName("deve retornar 400 quando Content-Type é inválido")
        void deveRetornar400ParaContentTypeInvalido() throws Exception {
            when(transcribeAudioPort.execute(any()))
                    .thenThrow(new AudioValidationException("file", "Content-Type não suportado: 'video/mp4'"));

            mockMvc.perform(multipart("/api/transcriptions")
                            .file(new MockMultipartFile("file", "video.mp4", "video/mp4", AUDIO_BYTES)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Arquivo de áudio inválido")))
                    .andExpect(jsonPath("$.field", is("file")));
        }

        @Test
        @DisplayName("deve retornar 400 quando arquivo excede 5 MB")
        void deveRetornar400QuandoArquivoGrande() throws Exception {
            when(transcribeAudioPort.execute(any()))
                    .thenThrow(new AudioValidationException("file", "Arquivo excede o tamanho máximo de 5 MB"));

            mockMvc.perform(multipart("/api/transcriptions")
                            .file(new MockMultipartFile("file", "grande.wav", "audio/wav", AUDIO_BYTES)))
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
            when(transcribeAudioPort.execute(any()))
                    .thenThrow(new SpeechToTextException("Speaches retornou erro 500"));

            mockMvc.perform(multipart("/api/transcriptions")
                            .file(new MockMultipartFile("file", "audio.wav", "audio/wav", AUDIO_BYTES)))
                    .andExpect(status().isBadGateway())
                    .andExpect(jsonPath("$.title", is("Falha no motor de transcrição")));
        }

        @Test
        @DisplayName("deve retornar 500 para exceção genérica")
        void deveRetornar500ParaExcecaoGenerica() throws Exception {
            when(transcribeAudioPort.execute(any()))
                    .thenThrow(new RuntimeException("Erro inesperado"));

            mockMvc.perform(multipart("/api/transcriptions")
                            .file(new MockMultipartFile("file", "audio.wav", "audio/wav", AUDIO_BYTES)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.title", is("Erro interno")));
        }
    }
}