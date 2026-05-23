package com.erichiroshi.speechaihexagonal.transcription.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AudioHashService")
class AudioHashServiceTest {

    @Test
    @DisplayName("deve gerar hash SHA-256 de 64 caracteres hexadecimais")
    void deveGerarHashDe64Chars() {
        byte[] audio = "conteudo-audio-fake".getBytes();
        String hash = AudioHashService.generate(audio);
        assertThat(hash).hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    @DisplayName("deve gerar o mesmo hash para os mesmos bytes")
    void deveSerDeterministico() {
        byte[] audio = "mesmo-conteúdo".getBytes();
        assertThat(AudioHashService.generate(audio)).isEqualTo(AudioHashService.generate(audio));
    }

    @Test
    @DisplayName("deve gerar hashes diferentes para conteúdos distintos")
    void deveGerarHashesDiferentesParaConteudosDiferentes() {
        String h1 = AudioHashService.generate("audio-a".getBytes());
        String h2 = AudioHashService.generate("audio-b".getBytes());
        assertThat(h1).isNotEqualTo(h2);
    }
}