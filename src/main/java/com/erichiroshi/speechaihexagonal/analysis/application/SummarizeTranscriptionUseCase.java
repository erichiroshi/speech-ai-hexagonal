package com.erichiroshi.speechaihexagonal.analysis.application;

import com.erichiroshi.speechaihexagonal.analysis.application.output.SummaryOutput;
import com.erichiroshi.speechaihexagonal.analysis.application.port.in.SummarizeTranscriptionPort;
import com.erichiroshi.speechaihexagonal.analysis.application.port.out.LanguageModelPort;
import com.erichiroshi.speechaihexagonal.analysis.application.port.out.SummaryStorePort;
import com.erichiroshi.speechaihexagonal.analysis.application.port.out.TranscriptionTextPort;
import com.erichiroshi.speechaihexagonal.analysis.domain.exception.AnalysisUnavailableException;
import com.erichiroshi.speechaihexagonal.analysis.domain.exception.TranscriptionNotFoundException;
import com.erichiroshi.speechaihexagonal.analysis.domain.model.Summary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Use Case de geração de resumo de transcrição via LLM.
 *
 * <p>Fluxo:
 * <ol>
 *   <li>Consulta cache Redis — retorna se resumo já existe (cached=true)</li>
 *   <li>Busca texto da transcrição via {@link TranscriptionTextPort}</li>
 *   <li>Monta prompt e chama LLM via {@link LanguageModelPort}</li>
 *   <li>Persiste resumo no cache e retorna (cached=false)</li>
 * </ol>
 *
 * <p>Completamente desacoplado de Ollama, Redis e do contexto transcription/.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SummarizeTranscriptionUseCase implements SummarizeTranscriptionPort {

    private final TranscriptionTextPort transcriptionTextPort;
    private final LanguageModelPort     languageModelPort;
    private final SummaryStorePort      summaryStorePort;

    @Value("${app.analysis.ollama.model}")
    private String model;

    @Override
    public SummaryOutput execute(String audioHash) {

        // 1. Cache hit — resumo já existe
        Optional<Summary> fromCache = summaryStorePort.findByAudioHash(audioHash);
        if (fromCache.isPresent()) {
            log.info("Cache hit (resumo) | audioHash={}", audioHash);
            return SummaryOutput.fromDomain(fromCache.get(), true);
        }

        // 2. Buscar texto da transcrição
        String transcriptionText = transcriptionTextPort.findTextByAudioHash(audioHash)
                .orElseThrow(() -> new TranscriptionNotFoundException(audioHash));

        log.info("Gerando resumo via LLM | audioHash={} | model={} | chars={}",
                audioHash, model, transcriptionText.length());

        // 3. Gerar resumo via LLM
        String prompt = buildPrompt(transcriptionText);
        String summaryText;
        try {
            summaryText = languageModelPort.generate(prompt);
        } catch (Exception ex) {
            log.error("Falha ao gerar resumo via LLM | audioHash={}", audioHash, ex);
            throw new AnalysisUnavailableException(
                    "Modelo de linguagem indisponível: " + ex.getMessage(), ex);
        }

        // 4. Persistir e retornar
        Summary summary = Summary.newSummary(audioHash, summaryText, model);
        summaryStorePort.save(summary);

        log.info("Resumo gerado e armazenado | audioHash={} | chars={}", audioHash, summaryText.length());
        return SummaryOutput.fromDomain(summary, false);
    }

    private String buildPrompt(String transcriptionText) {
        return """
                Você é um assistente especializado em resumir transcrições de áudio.
                Gere um resumo conciso e objetivo do seguinte texto transcrito.
                O resumo deve ter entre 3 e 5 frases, capturando os pontos principais.
                Responda apenas com o resumo, sem introduções ou explicações adicionais.

                Texto transcrito:
                %s
                """.formatted(transcriptionText);
    }
}
