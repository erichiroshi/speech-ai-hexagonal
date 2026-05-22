package com.erichiroshi.speechaihexagonal.transcription.application.port.in;

import com.erichiroshi.speechaihexagonal.transcription.application.input.TranscriptionInput;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;

public interface TranscribeAudioPort {

    TranscriptionOutput execute(TranscriptionInput input);

}
