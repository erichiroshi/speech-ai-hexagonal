package com.erichiroshi.speechaihexagonal.analysis.infrastructure.llm.ollama;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades do provider Ollama.
 *
 * <pre>
 * spring:
 *   ai:
 *     ollama:
 *       base-url: ${OLLAMA_BASE_URL:http://ollama:11434}
 *       chat:
 *         model: ${OLLAMA_MODEL:qwen2.5:0.5b}
 * app:
 *   analysis:
 *     model: ${OLLAMA_MODEL:qwen2.5:0.5b}
 * </pre>
 */
@ConfigurationProperties(prefix = "app.analysis.ollama")
public record OllamaProperties(
        String model,
        String baseUrl
) {
    public OllamaProperties {
        if (model == null || model.isBlank())     model   = "llama3.2:1b";
        if (baseUrl == null || baseUrl.isBlank()) baseUrl = "http://ollama:11434";
    }
}
