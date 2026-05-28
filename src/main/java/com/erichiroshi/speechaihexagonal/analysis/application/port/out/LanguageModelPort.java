package com.erichiroshi.speechaihexagonal.analysis.application.port.out;

/**
 * Driven Port — contrato de geração de texto via LLM.
 * O use case não conhece Ollama, OpenAI ou qualquer modelo específico.
 * Trocar de Ollama para OpenAI = novo adapter, zero mudança no use case.
 */
public interface LanguageModelPort {

    /**
     * Gera texto a partir de um prompt.
     *
     * @param prompt instrução completa para o modelo
     * @return texto gerado pelo modelo
     */
    String generate(String prompt);
}
