-- V1 — Criação da tabela de transcrições e índice de deduplicação por audioHash
-- Fase 3.7 — Persistência de Transcrições

CREATE TABLE transcriptions
(
    audio_hash    VARCHAR(64) NOT NULL,
    transcription TEXT        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_transcriptions PRIMARY KEY (audio_hash),
    CONSTRAINT uq_transcriptions_audio_hash UNIQUE (audio_hash)
);

-- Índice para deduplicação rápida por hash (usado no fluxo cache-first)
CREATE INDEX idx_transcriptions_audio_hash ON transcriptions (audio_hash);

COMMENT ON TABLE transcriptions IS 'Transcrições de áudio geradas por IA com deduplicação por SHA-256';
COMMENT ON COLUMN transcriptions.audio_hash IS 'SHA-256 hexadecimal do conteúdo binário do áudio — usado para deduplicação';
COMMENT ON COLUMN transcriptions.transcription IS 'Texto transcrito pelo motor de speech-to-text';
