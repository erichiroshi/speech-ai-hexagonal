package com.erichiroshi.speechaihexagonal.analysis.infrastructure.http;

import com.erichiroshi.speechaihexagonal.analysis.application.output.SummaryOutput;
import com.erichiroshi.speechaihexagonal.analysis.application.port.in.SummarizeTranscriptionPort;
import com.erichiroshi.speechaihexagonal.analysis.infrastructure.http.documentation.AnalysisControllerDocumentation;
import com.erichiroshi.speechaihexagonal.analysis.infrastructure.http.response.AnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Input Adapter REST para o contexto de análise.
 *
 * <p>Delega para {@link SummarizeTranscriptionPort} e retorna a resposta HTTP.
 * Sem lógica de negócio — comportamento idêntico ao TranscriptionController.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transcriptions")
public class AnalysisController implements AnalysisControllerDocumentation {

    private final SummarizeTranscriptionPort summarizeTranscriptionPort;

    /**
     * Gera ou recupera o resumo de uma transcrição existente.
     *
     * <p>Primeira chamada: ~5-30s (Ollama gerando)
     * <p>Chamadas seguintes: ~15ms (Redis cache hit)
     *
     * @param audioHash SHA-256 hexadecimal do áudio original
     */
    @PostMapping("/{audioHash}/analysis")
    public ResponseEntity<AnalysisResponse> analyze(@PathVariable String audioHash) {
        SummaryOutput output = summarizeTranscriptionPort.execute(audioHash);
        return ResponseEntity.ok(AnalysisResponse.fromOutput(output));
    }
}
