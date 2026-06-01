package com.erichiroshi.speechaihexagonal.transcription.domain.exception;

public class AudioValidationException extends RuntimeException {

    private final String field;

    public AudioValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
