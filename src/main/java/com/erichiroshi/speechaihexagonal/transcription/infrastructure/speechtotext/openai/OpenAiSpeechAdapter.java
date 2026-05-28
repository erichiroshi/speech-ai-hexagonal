package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.openai;

import com.erichiroshi.speechaihexagonal.transcription.application.port.out.SpeechToTextPort;
import com.erichiroshi.speechaihexagonal.transcription.domain.exception.SpeechToTextException;
import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

/**
 * Output Adapter — implementa {@link SpeechToTextPort} via Spring AI + OpenAI Whisper-1.
 *
 * <p>Ativado quando {@code speech.provider=openai} está configurado.
 * {@code @Primary} garante precedência sobre {@code SpeachesAdapter} quando ativo.
 *
 * <p>O domínio e os use cases não conhecem este adapter —
 * a troca de provider é transparente para toda a camada de aplicação.
 *
 * <p>Trade-off:
 * <ul>
 *   <li><b>Speaches (padrão):</b> gratuito, local, requer GPU, sem latência de rede</li>
 *   <li><b>OpenAI Whisper-1:</b> pago (~$0.006/min), cloud, sem infra local, SLA garantido</li>
 * </ul>
 *
 * <p>Ativar: {@code speech.provider=openai} em {@code application.yml}
 * ou via perfil: {@code --spring.profiles.active=dev,openai}
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(name = "speech.provider", havingValue = "openai")
@EnableConfigurationProperties(OpenAiSpeechProperties.class)
public class OpenAiSpeechAdapter implements SpeechToTextPort {

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final OpenAiSpeechProperties properties;

    @Override
    public Transcription transcribe(byte[] audioBytes, String fileName, String contentType) {
        log.info("Transcrevendo via OpenAI Whisper | model={} | filename={} | bytes={}",
                properties.model(), fileName, audioBytes.length);

        try {
            ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return (fileName != null && !fileName.isBlank()) ? fileName : "audio.wav";
                }
            };

            OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                    .model(properties.model())
                    .language("pt")
                    .build();

            AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, options);
            AudioTranscriptionResponse response = transcriptionModel.call(prompt);

            String text = response.getResult().getOutput();

            if (text.isBlank()) {
                throw new SpeechToTextException("OpenAI Whisper retornou resposta vazia");
            }

            log.info("Transcrição OpenAI concluída | chars={}", text.length());
            return Transcription.newTranscription("", text);

        } catch (SpeechToTextException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Falha na chamada à API OpenAI Whisper", ex);
            throw new SpeechToTextException("Falha ao transcrever via OpenAI: " + ex.getMessage(), ex);
        }
    }
}
