package com.erichiroshi.speechaihexagonal.transcription.application.output;

/**
 * Output do use case de transcrição — transporta o resultado para o adapter de entrada.
 */
public record TranscriptionOutput(String text) {
}
