package com.erichiroshi.speechaihexagonal.transcription.infrastructure.cache.inmemory;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription.newTranscription;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCacheAdapterTest {

    private static final String AUDIO_TRANSCRIPTION = "Audio transcrito fake";
    private InMemoryCacheAdapter cacheAdapter;
    private Map<String, Transcription> cacheMemory;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        cacheAdapter = new InMemoryCacheAdapter();
        // Acessa o mapa privado para validações específicas se necessário
        cacheMemory = (Map<String, Transcription>) ReflectionTestUtils.getField(cacheAdapter, "cacheMemory");
        if (cacheMemory != null) {
            cacheMemory.clear();
        }
    }

    @Test
    @DisplayName("Deve retornar a transcrição quando o hash existir no cache (Cache Hit)")
    void debeRetornarTranscricaoQuandoHashExistir() {
        String audioHash = "abc123hash";
        Transcription mockTranscription = newTranscription(audioHash, AUDIO_TRANSCRIPTION); // Certifique-se de preencher os campos se necessário
        cacheMemory.put(audioHash, mockTranscription);

        Optional<Transcription> resultado = cacheAdapter.findByAudioHash(audioHash);

        assertTrue(resultado.isPresent());
        assertEquals(mockTranscription, resultado.get());
    }

    @Test
    @DisplayName("Deve retornar Optional empty quando o hash não existir no cache (Cache Miss)")
    void deveRetornarOptionalEmptyQuandoHashNaoExistir() {
        String audioHash = "inexistente";

        Optional<Transcription> resultado = cacheAdapter.findByAudioHash(audioHash);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve salvar com sucesso uma nova transcrição no cache")
    void deveSalvarTranscricaoNoCache() {
        String audioHash = "xyz789hash";
        Transcription transcription = newTranscription(audioHash, AUDIO_TRANSCRIPTION);

        cacheAdapter.save(transcription);

        assertTrue(cacheMemory.containsKey(audioHash));
        assertEquals(transcription, cacheMemory.get(audioHash));
    }
}
