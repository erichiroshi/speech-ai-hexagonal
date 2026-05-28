package com.erichiroshi.speechaihexagonal.analysis.infrastructure.llm.ollama;

import com.erichiroshi.speechaihexagonal.analysis.application.port.out.LanguageModelPort;
import com.erichiroshi.speechaihexagonal.analysis.domain.exception.AnalysisUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Output Adapter — implementa {@link LanguageModelPort} via Spring AI + Ollama.
 *
 * <p>Usa {@link ChatClient} do Spring AI com modelo configurável.
 * Trocar para OpenAI = novo adapter implementando LanguageModelPort.
 * Use case e domínio não mudam uma linha.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(OllamaProperties.class)
public class OllamaLanguageModelAdapter implements LanguageModelPort {

    private final ChatClient chatClient;
    private final OllamaProperties properties;

    @Override
    public String generate(String prompt) {
        log.debug("Chamando Ollama | model={} | prompt_chars={}", properties.model(), prompt.length());

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                throw new AnalysisUnavailableException("Ollama retornou resposta vazia");
            }

            log.debug("Ollama respondeu | chars={}", response.length());
            return response.trim();

        } catch (AnalysisUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Falha na chamada ao Ollama | model={}", properties.model(), ex);
            throw new AnalysisUnavailableException(
                    "Ollama indisponível: " + ex.getMessage(), ex);
        }
    }
}
