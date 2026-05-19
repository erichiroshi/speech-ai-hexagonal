package com.erichiroshi.speechaihexagonal.transcription.application.mapper;

import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TranscriptionMapper {

    TranscriptionOutput toOutput(Transcription transcription);
}
