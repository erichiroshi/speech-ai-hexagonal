package com.erichiroshi.speechaihexagonal.transcription.application.input;

public record TranscriptionInput(
        byte[] audioBytes,
        String fileName,
        String contentType) {
}