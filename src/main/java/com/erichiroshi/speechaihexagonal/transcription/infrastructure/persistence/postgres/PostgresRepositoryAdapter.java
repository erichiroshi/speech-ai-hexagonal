package com.erichiroshi.speechaihexagonal.transcription.infrastructure.persistence.postgres;

import com.erichiroshi.speechaihexagonal.transcription.application.port.out.TranscriptionRepositoryPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.persistence.postgres.entity.TranscriptionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class PostgresRepositoryAdapter implements TranscriptionRepositoryPort {

    private final TranscriptionJpaRepository repository;

    @Override
    public Optional<Transcription> findByAudioHash(String audioHash) {

        log.debug("Buscando transcrição no banco (Postgres) | audioHash={}", audioHash);

        return repository.findByAudioHash(audioHash)
                .map(transcriptionEntity -> {
                    log.info("Transcription existente no banco (Postgres) | audioHash={}", audioHash);
                    return transcriptionEntity.toDomain();
                });
    }

    @Override
    public Transcription save(Transcription transcription) {

        log.debug("Salvando transcrição no banco (Postgres)");

        TranscriptionEntity entity = TranscriptionEntity.toEntity(transcription);
        TranscriptionEntity saved = repository.save(entity);

        log.info("Transcrição salva no banco (Postgres) {}", saved.getAudioHash());


        return saved.toDomain();
    }
}
