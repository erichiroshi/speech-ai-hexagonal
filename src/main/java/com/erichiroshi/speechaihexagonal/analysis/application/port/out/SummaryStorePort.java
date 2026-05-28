package com.erichiroshi.speechaihexagonal.analysis.application.port.out;

import com.erichiroshi.speechaihexagonal.analysis.domain.model.Summary;

import java.util.Optional;

/**
 * Driven Port — contrato de armazenamento de resumos.
 * Desacoplado de Redis ou qualquer tecnologia de cache.
 */
public interface SummaryStorePort {

    Optional<Summary> findByAudioHash(String audioHash);

    void save(Summary summary);
}
