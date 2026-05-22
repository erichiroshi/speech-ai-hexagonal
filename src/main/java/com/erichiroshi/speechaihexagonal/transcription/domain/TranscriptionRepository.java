package com.erichiroshi.speechaihexagonal.transcription.domain;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;

import java.util.Optional;

public interface TranscriptionRepository {

    Optional<Transcription> findByAudioHash(String audioHash);

    Transcription save(Transcription transcription);
}
