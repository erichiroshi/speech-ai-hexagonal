package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.exception;

import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final String field;

    public InvalidRequestException(String field, String message) {
        super(message);
        this.field = field;
    }

}
