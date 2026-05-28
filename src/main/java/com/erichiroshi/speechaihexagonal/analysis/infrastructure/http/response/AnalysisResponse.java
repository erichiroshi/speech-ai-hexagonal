package com.erichiroshi.speechaihexagonal.analysis.infrastructure.http.response;

import com.erichiroshi.speechaihexagonal.analysis.application.output.SummaryOutput;

import java.time.Instant;
import java.util.UUID;

/**
 * Response HTTP do endpoint de análise.
 */
public record AnalysisResponse(
        UUID    id,
        String  audioHash,
        String  summary,
        String  model,
        boolean cached,
        Instant createdAt
) {
    public static AnalysisResponse fromOutput(SummaryOutput output) {
        return new AnalysisResponse(
                output.id(),
                output.audioHash(),
                output.summary(),
                output.model(),
                output.cached(),
                output.createdAt()
        );
    }
}
