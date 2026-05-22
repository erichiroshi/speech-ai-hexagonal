package com.erichiroshi.speechaihexagonal.transcription.infrastructure.persistence.postgres.entity;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transcriptions")
public class TranscriptionEntity {

    @Id
    @Column(name = "audio_hash", nullable = false, unique = true, length = 64)
    private String audioHash;

    @Column(name = "transcription", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public TranscriptionEntity(String audioHash, String text) {
        this.audioHash = audioHash;
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }

    public static TranscriptionEntity toEntity(Transcription transcription) {
       return new TranscriptionEntity(transcription.getAudioHash(), transcription.getText());
    }

    public Transcription toDomain() {
        return new Transcription(audioHash, text);
    }

}
