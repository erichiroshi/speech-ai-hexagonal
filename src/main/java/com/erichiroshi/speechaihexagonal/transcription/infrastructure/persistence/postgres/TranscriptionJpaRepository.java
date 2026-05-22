package com.erichiroshi.speechaihexagonal.transcription.infrastructure.persistence.postgres;

import com.erichiroshi.speechaihexagonal.transcription.infrastructure.persistence.postgres.entity.TranscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TranscriptionJpaRepository extends JpaRepository<TranscriptionEntity, UUID> {

    Optional<TranscriptionEntity> findByAudioHash(String audioHash);

}
