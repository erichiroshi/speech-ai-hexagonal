package com.erichiroshi.speechaihexagonal.transcription.domain.exception;

public class SpeechToTextException extends RuntimeException {

    public SpeechToTextException(String message) {
        super(message);
    }

    public SpeechToTextException(String message, Throwable cause) {
        super(message, cause);
    }
}
