package com.erichiroshi.speechaihexagonal.analysis.infrastructure.http;

import com.erichiroshi.speechaihexagonal.analysis.application.output.SummaryOutput;
import com.erichiroshi.speechaihexagonal.analysis.application.port.in.SummarizeTranscriptionPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
@DisplayName("AnalysisController")
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SummarizeTranscriptionPort summarizeTranscriptionPort;

    private static final UUID FIXED_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String AUDIO_HASH = "a".repeat(64);
    private static final String SUMMARY = "Resumo fake de trancription";
    private static final String MODEL = "qwen2.5:0.5b";
    private static final boolean CACHED = true;
    private static final Instant FIXED_TIME = Instant.parse("2025-01-01T10:00:00Z");

    private static SummaryOutput fakeOutput() {
        return new SummaryOutput(FIXED_ID, AUDIO_HASH, SUMMARY, MODEL, CACHED, FIXED_TIME);
    }

    @Test
    @DisplayName("Deve retornar status 200 e o resumo da transcrição com sucesso")
    void deveRetornarSucessoAoAnalisarTranscricao() throws Exception {
        // Arrange
        String audioHash = "a1b2c3d4e5f6g7h8i9j0";
        SummaryOutput mockOutput = fakeOutput();

        when(summarizeTranscriptionPort.execute(audioHash)).thenReturn(mockOutput);

        // Act & Assert
        mockMvc.perform(post("/api/transcriptions/{audioHash}/analysis", audioHash)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.audioHash").value(mockOutput.audioHash()))
                .andExpect(jsonPath("$.summary").value(mockOutput.summary()))
                .andExpect(jsonPath("$.model").value(mockOutput.model()));
    }
}
