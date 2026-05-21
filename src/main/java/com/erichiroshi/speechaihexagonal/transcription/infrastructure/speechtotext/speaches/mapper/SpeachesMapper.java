package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.mapper;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.speaches.response.SpeachesResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpeachesMapper {

    // Provide a default implementation so MapStruct won't try to pick an ambiguous constructor
    default Transcription toDomain(SpeachesResponse speachesResponse) {
        if (speachesResponse == null) {
            return null;
        }
        // Use the domain factory method to create the Transcription instance
        return Transcription.newTranscription(null, speachesResponse.text());
    }

}
