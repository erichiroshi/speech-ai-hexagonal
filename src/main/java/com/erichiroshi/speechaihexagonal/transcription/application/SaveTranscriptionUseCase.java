package com.erichiroshi.speechaihexagonal.transcription.application;

import com.erichiroshi.speechaihexagonal.transcription.application.input.SaveTranscriptionCommand;
import com.erichiroshi.speechaihexagonal.transcription.application.mapper.TranscriptionMapper;
import com.erichiroshi.speechaihexagonal.transcription.application.output.TranscriptionOutput;
import com.erichiroshi.speechaihexagonal.transcription.application.port.in.SaveTranscriptionPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import com.erichiroshi.speechaihexagonal.transcription.domain.port.out.TranscriptionGateway;
import com.erichiroshi.speechaihexagonal.transcription.domain.service.AudioHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SaveTranscriptionUseCase implements SaveTranscriptionPort {

    private final TranscriptionGateway transcriptionGateway;
    private final TranscriptionMapper transcriptionMapper;

    @Transactional
    @Override
    public TranscriptionOutput execute(SaveTranscriptionCommand command) {

        String audioHash = AudioHashService.generate(command.audioBytes());

        log.info("Persistindo transcrição | audioHash{}", audioHash);

        Transcription transcription = Transcription.newTranscription(audioHash, command.text());

        Transcription saved = transcriptionGateway.save(transcription);

        log.info("Transcrição persistida | id={} | audioHash={}", saved.getId(), audioHash);

        return transcriptionMapper.toOutput(saved);
    }
}
