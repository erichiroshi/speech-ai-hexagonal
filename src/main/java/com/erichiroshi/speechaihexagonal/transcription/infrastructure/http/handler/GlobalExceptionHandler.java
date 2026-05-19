package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.handler;

import com.erichiroshi.speechaihexagonal.transcription.domain.exception.AudioValidationException;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_URI = "/errors/";

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

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado");
        problem.setType(URI.create(BASE_URI + "internal"));
        problem.setTitle("Erro interno");
        return problem;
    }

}
