package com.erichiroshi.speechaihexagonal.transcription.application.port.in;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;

/**
 * Porta de entrada (driving port) do bounded context de transcrição.
 * Define o contrato que o adapter de entrada (Controller) deve invocar.
 * Implementada por TranscribeAudioUseCase.
 */
public interface TranscribeAudioPort {

    TranscriptionOutput execute(TranscriptionInput input);
}
