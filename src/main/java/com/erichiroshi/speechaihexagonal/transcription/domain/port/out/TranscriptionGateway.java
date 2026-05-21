package com.erichiroshi.speechaihexagonal.transcription.domain.port.out;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;

import java.util.Optional;

/**
 * Driven Port — contrato de persistência orientado ao domínio.
 * A aplicação depende desta interface; a infraestrutura a implementa.
 * Sem acoplamento a JPA, PostgreSQL ou qualquer framework de persistência.
 */
public interface TranscriptionGateway {

    /**
     * Busca transcrição existente pelo hash SHA-256 do áudio.
     * Utilizado para deduplicação antes de chamar a IA.
     */
    Optional<Transcription> findByAudioHash(String audioHash);

    /**
     * Persiste uma nova transcrição.
     */
    Transcription save(Transcription transcription);
}
