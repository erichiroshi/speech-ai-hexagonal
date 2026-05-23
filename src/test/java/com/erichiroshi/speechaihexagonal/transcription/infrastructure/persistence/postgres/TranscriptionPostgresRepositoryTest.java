package com.erichiroshi.speechaihexagonal.transcription.infrastructure.persistence.postgres;

import com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static com.erichiroshi.speechaihexagonal.transcription.domain.model.Transcription.newTranscription;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TranscriptionPostgresRepository.class)
@Transactional
@DisplayName("PostgresTranscriptionRepository Test com Testcontainers")
class TranscriptionPostgresRepositoryTest {

    @Autowired
    private TranscriptionPostgresRepository postgresRepository;

    private static final String VALID_HASH = "a".repeat(64);
    private static final String VALID_TEXT = "Texto transcrito com sucesso via banco real.";

    @Nested
    @DisplayName("Cenários de salvamento (save)")
    class CenariosDeSalvamento {

        @Test
        @DisplayName("Deve salvar o registro no banco PostgreSQL real e retornar o domínio")
        void deveSalvarTranscriptionComSucesso() {
            Transcription domainModel = newTranscription(VALID_HASH, VALID_TEXT);

            Transcription savedDomain = postgresRepository.save(domainModel);

            assertThat(savedDomain).isNotNull();
            assertThat(savedDomain.getAudioHash()).isEqualTo(VALID_HASH);
            assertThat(savedDomain.getText()).isEqualTo(VALID_TEXT);
            assertThat(savedDomain.getCreatedAt()).isNotNull();

            // Validação física de leitura na tabela do PostgreSQL
            Optional<Transcription> dbCheck = postgresRepository.findByAudioHash(VALID_HASH);

            assertThat(dbCheck).isPresent();
            assertThat(dbCheck.get().getText()).isEqualTo(VALID_TEXT);
            assertThat(dbCheck.get().getId()).isEqualTo(savedDomain.getId());
        }
    }

    @Nested
    @DisplayName("Cenários de Busca (findByAudioHash)")
    class CenariosDeBusca {

        @Test
        @DisplayName("Deve retornar a transcrição mapeada para o domínio quando existir no Postgres")
        void deveRetornarTranscriptionQuandoHashExistir() {
            Transcription domainModel = newTranscription(VALID_HASH, VALID_TEXT);
            postgresRepository.save(domainModel);

            Optional<Transcription> result = postgresRepository.findByAudioHash(VALID_HASH);

            assertThat(result).isPresent();
            assertThat(result.get().getAudioHash()).isEqualTo(VALID_HASH);
            assertThat(result.get().getText()).isEqualTo(VALID_TEXT);
        }

        @Test
        @DisplayName("Deve retornar Optional vazio quando o hash buscado não existir no Postgres")
        void deveRetornarOptionalVazioQuandoHashNaoExistir() {
            Optional<Transcription> result = postgresRepository.findByAudioHash("hash-inexistente-no-postgres");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("deve retornar vazio para hash de áudio diferente")
        void deveRetornarVazioParaHashDiferente() {
            Transcription saved = Transcription.newTranscription("b".repeat(64), "outro texto");
            postgresRepository.save(saved);

            Optional<Transcription> result = postgresRepository.findByAudioHash("c".repeat(64));
            assertThat(result).isEmpty();
        }
    }
}
