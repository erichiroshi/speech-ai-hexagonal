package com.erichiroshi.speechaihexagonal.analysis.application.port.out;

import java.util.Optional;

/**
 * Driven Port — contrato de consulta de texto de transcrição.
 *
 * <p>O bounded context analysis/ busca o texto transcrito sem conhecer
 * detalhes do contexto transcription/ (sem importar classes de outro contexto).
 * A infraestrutura implementa esta porta acessando o Redis compartilhado.
 */
public interface TranscriptionTextPort {

    /**
     * Busca o texto transcrito pelo audioHash.
     * Consulta Redis primeiro, depois PostgreSQL.
     */
    Optional<String> findTextByAudioHash(String audioHash);
}
