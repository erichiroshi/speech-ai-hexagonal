package com.erichiroshi.speechaihexagonal.transcription.application.port.out;

import java.util.function.Supplier;

public interface TranscriptionMetricsPort {
    void recordSuccess();
    void recordError();
    void recordCacheHitRedis();
    void recordCacheHitDb();
    void recordAiCall();
    void recordFileSize(long bytes);
    <T> T timeSpeaches(Supplier<T> supplier);
}