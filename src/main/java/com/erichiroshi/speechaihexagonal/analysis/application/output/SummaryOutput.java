package com.erichiroshi.speechaihexagonal.analysis.application.output;

import com.erichiroshi.speechaihexagonal.analysis.domain.model.Summary;

import java.time.Instant;
import java.util.UUID;

/**
 * Output do caso de uso de resumo — transporte para o adapter de entrada.
 *
 * @param cached true quando o resumo foi recuperado do cache (Redis hit)
 */
public record SummaryOutput(
        UUID    id,
        String  audioHash,
        String  summary,
        String  model,
        boolean cached,
        Instant createdAt
) {
    public static SummaryOutput fromDomain(Summary summary, boolean cached) {
        return new SummaryOutput(
                summary.getId(),
                summary.getAudioHash(),
                summary.getText(),
                summary.getModel(),
                cached,
                summary.getCreatedAt()
        );
    }
}
