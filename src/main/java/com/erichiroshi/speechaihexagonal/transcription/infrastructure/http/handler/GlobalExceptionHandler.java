package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.handler;

import com.erichiroshi.speechaihexagonal.analysis.domain.exception.AnalysisUnavailableException;
import com.erichiroshi.speechaihexagonal.analysis.domain.exception.TranscriptionNotFoundException;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_URI = "/errors/";

    // ── transcription/ ────────────────────────────────────────────────────────
    @ExceptionHandler(AudioValidationException.class)
    public ProblemDetail handleAudioValidation(AudioValidationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create(BASE_URI + "audio-validation"));
        problem.setTitle("Arquivo de áudio inválido");
        problem.setProperty("field", ex.getField());
        return problem;
    }

    @ExceptionHandler(SpeechToTextException.class)
    public ProblemDetail handleSpeechToText(SpeechToTextException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        problem.setType(URI.create(BASE_URI + "speech-to-text"));
        problem.setTitle("Falha no motor de transcrição");
        return problem;
    }

    /**
     * CircuitBreaker OPEN — Speaches temporariamente indisponível.
     * Retorna 503 Service Unavailable com Retry-After header.
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ProblemDetail handleCircuitBreakerOpen(CallNotPermittedException ex) {
        log.warn("CircuitBreaker [{}] OPEN — chamada rejeitada", ex.getCausingCircuitBreakerName());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Serviço de transcrição temporariamente indisponível. Tente novamente em instantes.");
        problem.setType(URI.create(BASE_URI + "circuit-open"));
        problem.setTitle("Serviço indisponível");
        return problem;
    }

    /**
     * Bulkhead cheio — muitas requisições simultâneas.
     */
    @ExceptionHandler(BulkheadFullException.class)
    public ProblemDetail handleBulkheadFull(BulkheadFullException ex) {
        log.warn("Bulkhead [speaches] cheio — requisição rejeitada");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "Muitas requisições simultâneas. Tente novamente em instantes.");
        problem.setType(URI.create(BASE_URI + "bulkhead-full"));
        problem.setTitle("Capacidade excedida");
        return problem;
    }

    // ── analysis/ ─────────────────────────────────────────────────────────────
    @ExceptionHandler(TranscriptionNotFoundException.class)
    public ProblemDetail handleTranscriptionNotFound(TranscriptionNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create(BASE_URI + "transcription-not-found"));
        problem.setTitle("Transcrição não encontrada");
        problem.setProperty("audioHash", ex.getAudioHash());
        return problem;
    }

    @ExceptionHandler(AnalysisUnavailableException.class)
    public ProblemDetail handleAnalysisUnavailable(AnalysisUnavailableException ex) {
        log.error("Modelo de linguagem indisponível", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problem.setType(URI.create(BASE_URI + "analysis-unavailable"));
        problem.setTitle("Serviço de análise indisponível");
        return problem;
    }

    // ── genérico ──────────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado");
        problem.setType(URI.create(BASE_URI + "internal"));
        problem.setTitle("Erro interno");
        return problem;
    }

}
