package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.mapper;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.response.SpeachesResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpeachesMapper {

    Transcription toDomain(SpeachesResponse speachesResponse);

}
