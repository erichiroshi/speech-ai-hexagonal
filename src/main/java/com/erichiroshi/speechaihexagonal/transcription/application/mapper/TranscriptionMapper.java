package com.erichiroshi.speechaihexagonal.transcription.application.mapper;

import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import org.mapstruct.Mapper;

/**
 * Mapper de domínio → output da camada de aplicação.
 * Mantido na camada application pois mapeia objetos de domínio.
 */
@Mapper(componentModel = "spring")
public interface TranscriptionMapper {

    TranscriptionOutput toOutput(Transcription transcription);
}
