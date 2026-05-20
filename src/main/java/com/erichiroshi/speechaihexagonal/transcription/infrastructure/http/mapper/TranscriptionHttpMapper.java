package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.mapper;

import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.response.TranscriptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper da camada HTTP: {@link TranscriptionOutput} → {@link TranscriptionResponse}.
 * Mantido na infra, pois mapeia objetos de apresentação HTTP.
 */
@Mapper(componentModel = "spring")
public interface TranscriptionHttpMapper {

    @Mapping(target = "audioTranscription", source = "text")
    TranscriptionResponse toResponse(TranscriptionOutput output);
}
