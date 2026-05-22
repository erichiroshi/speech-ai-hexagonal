package com.erichiroshi.speechaihexagonal.transcription.domain;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;

public interface SpeechToTextPort {

    Transcription transcribe(byte[] audioBytes, String fileName, String contentType);
}
