package com.erichiroshi.speechaihexagonal.transcription.infrastructure.speechtotext.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades do provider OpenAI Whisper.
 *
 * <pre>
 * spring:
 *   ai:
 *     openai:
 *       api-key: ${OPENAI_API_KEY}
 *       audio:
 *         transcription:
 *           options:
 *             model: whisper-1
 *             language: pt
 *             response-format: json
 * </pre>
 */
@ConfigurationProperties(prefix = "app.openai")
public record OpenAiSpeechProperties(
        String model
) {
    public OpenAiSpeechProperties {
        if (model == null || model.isBlank()) {
            model = "whisper-1";
        }
    }
}
