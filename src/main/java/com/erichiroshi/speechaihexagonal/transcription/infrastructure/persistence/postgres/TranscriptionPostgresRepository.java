package com.erichiroshi.speechaihexagonal.transcription.infrastructure.persistence.postgres;

import com.erichiroshi.speechaihexagonal.transcription.domain.TranscriptionRepository;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.persistence.postgres.entity.TranscriptionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Repository
public class TranscriptionPostgresRepository implements TranscriptionRepository {

    private final TranscriptionJpaRepository repository;

    @Override
    public Optional<Transcription> findByAudioHash(String audioHash) {

        log.debug("Buscando transcrição no banco | audioHash={}", audioHash);

        return repository.findByAudioHash(audioHash)
                .map(TranscriptionEntity::toDomain);
    }

    @Override
    public Transcription save(Transcription transcription) {

        TranscriptionEntity entity = TranscriptionEntity.toEntity(transcription);
        TranscriptionEntity saved = repository.save(entity);

        log.debug("Transcrição salva no banco {}", saved.getAudioHash());

        return saved.toDomain();
    }
}
