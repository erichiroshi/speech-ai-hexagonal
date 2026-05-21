package com.erichiroshi.speechaihexagonal.transcription.application.port.in;

import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;

import java.util.Optional;

/**
 * Driver Port — expõe o caso de uso de recuperação de transcrição por audioHash.
 */
public interface GetTranscriptionByHashPort {
    Optional<TranscriptionOutput> execute(String audioHash);
}