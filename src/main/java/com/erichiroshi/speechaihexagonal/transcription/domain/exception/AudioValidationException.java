package com.erichiroshi.speechaihexagonal.transcription.domain.exception;

import lombok.Getter;

@Getter
public class AudioValidationException extends RuntimeException {

    private final String field;

    public AudioValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

}
