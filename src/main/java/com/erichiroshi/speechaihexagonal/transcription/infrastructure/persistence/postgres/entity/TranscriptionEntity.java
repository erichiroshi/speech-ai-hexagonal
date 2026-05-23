package com.erichiroshi.speechaihexagonal.transcription.infrastructure.persistence.postgres.entity;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.TranscriptionId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transcriptions")
public class TranscriptionEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "audio_hash", nullable = false, unique = true, length = 64)
    private String audioHash;

    @Column(name = "transcription", nullable = false, columnDefinition = "TEXT")
    private String transcription;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static TranscriptionEntity toEntity(Transcription transcription) {
        return new TranscriptionEntity(
                transcription.getId().id(),
                transcription.getAudioHash(),
                transcription.getText(),
                transcription.getCreatedAt());
    }

    public Transcription toDomain() {
        return new Transcription(new TranscriptionId(id), audioHash, transcription, createdAt);
    }

}
