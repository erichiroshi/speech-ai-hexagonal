package com.erichiroshi.speechaihexagonal.transcription.application.input;

import java.util.Arrays;
import java.util.Objects;

public record TranscriptionInput(
        byte[] audioBytes,
        String fileName,
        String contentType) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TranscriptionInput that = (TranscriptionInput) o;
        return Objects.equals(fileName(), that.fileName())
                && Objects.deepEquals(audioBytes(), that.audioBytes())
                && Objects.equals(contentType(), that.contentType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(audioBytes()), fileName(), contentType());
    }

    @Override
    public String toString() {
        return "TranscriptionInput{" +
                "audioBytes=" + Arrays.toString(audioBytes) +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}
