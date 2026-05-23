package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.inmemory;

import com.erichiroshi.speechaihexagonal.transcription.domain.TranscriptionCachePort;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Profile("test")
@Slf4j
@RequiredArgsConstructor
@Component
public class InMemoryCacheAdapter implements TranscriptionCachePort {

    private final Map<String, Transcription> cacheMemory = new HashMap<>();

    @Override
    public Optional<Transcription> get(String audioHash) {
        Transcription transcription = cacheMemory.get(audioHash);
        if (transcription != null) {
            log.info("Cache InMemory hit, audioHash={}", audioHash);
            return  Optional.of(transcription);
        }
        return Optional.empty();
    }

    @Override
    public void put(String audioHash, Transcription transcription) {
        log.info("Salvando cache InMemory, audioHash={}", audioHash);
        cacheMemory.put(audioHash, transcription);
    }
}
