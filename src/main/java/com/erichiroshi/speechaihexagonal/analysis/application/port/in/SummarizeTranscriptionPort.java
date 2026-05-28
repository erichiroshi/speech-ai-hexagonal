package com.erichiroshi.speechaihexagonal.analysis.application.port.in;

import com.erichiroshi.speechaihexagonal.analysis.application.output.SummaryOutput;

/**
 * Driver Port — expõe o caso de uso de geração de resumo via LLM.
 */
public interface SummarizeTranscriptionPort {

    SummaryOutput execute(String audioHash);
}
