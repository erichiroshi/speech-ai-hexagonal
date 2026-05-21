package com.erichiroshi.speechaihexagonal.transcription.application.input;

/**
 * Comando de entrada para o use case de persistência.
 * Carrega os bytes do áudio (para geração do hash) e o texto transcrito.
 *
 * @param audioBytes bytes do áudio original
 * @param text       texto resultante da transcrição
 */
public record SaveTranscriptionCommand(byte[] audioBytes, String text) {
}
