package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.handler;

import com.erichiroshi.speechaihexagonal.analysis.domain.exception.AnalysisUnavailableException;
import com.erichiroshi.speechaihexagonal.analysis.domain.exception.TranscriptionNotFoundException;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // ── transcription/ ────────────────────────────────────────────────────────
    @Test
    void deveTratarAudioValidationException() {
        // Cenário
        AudioValidationException exception = new AudioValidationException("formato", "Formato inválido");

        // Ação
        ProblemDetail result = handler.handleAudioValidation(exception);

        // Verificação
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Formato inválido", result.getDetail());
        assertEquals(URI.create("/errors/audio-validation"), result.getType());
        assertEquals("Arquivo de áudio inválido", result.getTitle());
        assert result.getProperties() != null;
        assertEquals("formato", result.getProperties().get("field"));
    }

    @Test
    void deveTratarSpeechToTextException() {
        // Cenário
        SpeechToTextException exception = new SpeechToTextException("Timeout da API externa");

        // Ação
        ProblemDetail result = handler.handleSpeechToText(exception);

        // Verificação
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_GATEWAY.value(), result.getStatus());
        assertEquals("Timeout da API externa", result.getDetail());
        assertEquals(URI.create("/errors/speech-to-text"), result.getType());
        assertEquals("Falha no motor de transcrição", result.getTitle());
    }

    @Test
    void deveTratarCallNotPermittedExceptionQuandoCircuitBreakerAberto() {
        // Cenário - Configura o mock para não estourar NullPointerException
        CircuitBreaker circuitBreaker = mock(CircuitBreaker.class);
        CircuitBreakerConfig config = mock(CircuitBreakerConfig.class);

        when(circuitBreaker.getName()).thenReturn("transcriptionService");
        when(circuitBreaker.getCircuitBreakerConfig()).thenReturn(config);
        when(config.isWritableStackTraceEnabled()).thenReturn(true);

        CallNotPermittedException exception = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);

        // Ação
        ProblemDetail result = handler.handleCircuitBreakerOpen(exception);

        // Verificação
        assertNotNull(result);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), result.getStatus());
        assertEquals("Serviço de transcrição temporariamente indisponível. Tente novamente em instantes.", result.getDetail());
        assertEquals(URI.create("/errors/circuit-open"), result.getType());
        assertEquals("Serviço indisponível", result.getTitle());
    }

    @Test
    void deveTratarBulkheadFullException() {
        // Cenário - Configura o mock para não estourar NullPointerException
        Bulkhead bulkhead = mock(Bulkhead.class);
        BulkheadConfig config = mock(BulkheadConfig.class);

        when(bulkhead.getBulkheadConfig()).thenReturn(config);
        when(config.isWritableStackTraceEnabled()).thenReturn(true);

        BulkheadFullException exception = BulkheadFullException.createBulkheadFullException(bulkhead);

        // Ação
        ProblemDetail result = handler.handleBulkheadFull(exception);

        // Verificação
        assertNotNull(result);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), result.getStatus());
        assertEquals("Muitas requisições simultâneas. Tente novamente em instantes.", result.getDetail());
        assertEquals(URI.create("/errors/bulkhead-full"), result.getType());
        assertEquals("Capacidade excedida", result.getTitle());
    }

    // ── analysis/ ─────────────────────────────────────────────────────────────
    @Test
    void deveTratarTranscriptionNotFoundException() {
        // Cenário
        String audioHash = "a1b2c3d4";
        TranscriptionNotFoundException exception = new TranscriptionNotFoundException(audioHash);

        // Ação
        ProblemDetail result = handler.handleTranscriptionNotFound(exception);

        // Verificação
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
        assertEquals("Transcrição não encontrada para audioHash: " + audioHash, result.getDetail());
        assertEquals("Transcrição não encontrada", result.getTitle());
        assert result.getProperties() != null;
        assertEquals(audioHash, result.getProperties().get("audioHash"));
         assertEquals(URI.create("/errors/transcription-not-found"), result.getType());
    }

    @Test
    void deveTratarAnalysisUnavailableException() {
        // Cenário
        AnalysisUnavailableException exception = new AnalysisUnavailableException("Ollama offline");

        // Ação
        ProblemDetail result = handler.handleAnalysisUnavailable(exception);

        // Verificação
        assertNotNull(result);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), result.getStatus());
        assertEquals("Ollama offline", result.getDetail());
        assertEquals("Serviço de análise indisponível", result.getTitle());
    }

    // ── genérico ──────────────────────────────────────────────────────────────
    @Test
    void deveTratarGenericException() {
        // Cenário
        RuntimeException exception = new RuntimeException("NullPointer inesperado");

        // Ação
        ProblemDetail result = handler.handleGeneric(exception);

        // Verificação
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Erro interno inesperado", result.getDetail());
        assertEquals(URI.create("/errors/internal"), result.getType());
        assertEquals("Erro interno", result.getTitle());
    }
}
