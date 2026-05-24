package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.inmemory;

import com.erichiroshi.speechaihexagonal.transcription.domain.TranscriptionCachePort;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Profile("test")
@Slf4j
@RequiredArgsConstructor
@Component
public class InMemoryCacheAdapter implements TranscriptionCachePort {

    private final Map<String, Transcription> cacheMemory = new ConcurrentHashMap<>();

    @Override
    public Optional<Transcription> findByAudioHash(String audioHash) {
        Transcription transcription = cacheMemory.get(audioHash);
        if (transcription != null) {
            log.info("Cache HIT (InMemory), audioHash={}", audioHash);
            return  Optional.of(transcription);
        }
        log.info("Cache miss (InMemory)) | key={}", audioHash);
        return Optional.empty();
    }

    @Override
    public void save(Transcription transcription) {
        log.info("Salvando no cache (InMemory), audioHash={}", transcription.getAudioHash());
        cacheMemory.put(transcription.getAudioHash(), transcription);
    }
}
