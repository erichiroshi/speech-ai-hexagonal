package com.erichiroshi.speechaihexagonal.analysis.domain.exception;

/**
 * Lançada quando não há transcrição disponível para o audioHash informado.
 * O resumo não pode ser gerado sem o texto original.
 */
public class TranscriptionNotFoundException extends RuntimeException {

    private final String audioHash;

    public TranscriptionNotFoundException(String audioHash) {
        super("Transcrição não encontrada para audioHash: " + audioHash);
        this.audioHash = audioHash;
    }

    public String getAudioHash() {
        return audioHash;
    }
}
