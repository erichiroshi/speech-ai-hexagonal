package com.erichiroshi.speechaihexagonal.transcription.domain;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;

import java.util.Optional;

public interface TranscriptionCachePort {

    Optional<Transcription> get(String audioHash);

    void put(String audioHash, Transcription transcription);
}
