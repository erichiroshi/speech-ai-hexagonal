package com.erichiroshi.speechaihexagonal.analysis.domain.exception;

/**
 * Lançada quando o modelo de linguagem não está disponível ou falha
 * ao gerar o resumo.
 */
public class AnalysisUnavailableException extends RuntimeException {

    public AnalysisUnavailableException(String message) {
        super(message);
    }

    public AnalysisUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
