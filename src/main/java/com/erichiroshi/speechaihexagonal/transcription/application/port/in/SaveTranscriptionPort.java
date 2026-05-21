package com.erichiroshi.speechaihexagonal.transcription.application.port.in;

import com.erichiroshi.speechaihexagonal.transcription.application.input.SaveTranscriptionCommand;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;

/**
 * Driver Port — expõe o caso de uso de persistência de transcrição.
 */
public interface SaveTranscriptionPort {
    TranscriptionOutput execute(SaveTranscriptionCommand input);
}