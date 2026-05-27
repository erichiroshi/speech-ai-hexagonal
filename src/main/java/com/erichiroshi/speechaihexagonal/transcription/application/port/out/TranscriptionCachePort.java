package com.erichiroshi.speechaihexagonal.transcription.application.port.out;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;

import java.util.Optional;

public interface TranscriptionCachePort {

    Optional<Transcription> findByAudioHash(String audioHash);

    void save(Transcription transcription);
}
