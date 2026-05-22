package com.erichiroshi.speechaihexagonal.transcription.application.output;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;

public record TranscriptionOutput(
        String audioHash,
        String text) {

    public static TranscriptionOutput toOutput(Transcription transcription) {
        return new TranscriptionOutput(transcription.getAudioHash(), transcription.getText());
    }

    public Transcription toDomain() {
        return new Transcription(audioHash, text);
    }
}
