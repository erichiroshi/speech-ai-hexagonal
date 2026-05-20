package com.erichiroshi.speechaihexagonal.transcription.domain.port.out;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;

/**
 * Porta de saída (driven port) para o motor de transcrição de áudio.
 *
 * <p>Implementações previstas:
 * <ul>
 *   <li>{@code SpeachesAdapter} — Fase 1: Whisper local via Docker (Speaches)</li>
 *   <li>{@code OpenAiSpeechAdapter} — Fase 5: OpenAI Whisper-1 via Spring AI</li>
 * </ul>
 *
 * <p>Trocar o motor = novo adapter. O domínio e o use case não mudam.
 */
public interface SpeechToTextPort {

    /**
     * Transcreve os bytes de um arquivo de áudio.
     *
     * @param audioBytes  conteúdo binário do arquivo
     * @param fileName    nome original do arquivo (usado no multipart)
     * @param contentType Content-Type declarado (ex: audio/wav)
     * @return {@link Transcription} com o texto resultante
     */
    Transcription transcribe(byte[] audioBytes, String fileName, String contentType);
}
