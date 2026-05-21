package com.erichiroshi.speechaihexagonal.transcription.domain.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Serviço de domínio responsável pela geração determinística do audioHash.
 * Utiliza SHA-256 hexadecimal sobre o conteúdo binário do áudio.
 *
 * <p>Java puro — sem dependência de frameworks.
 * Reutilizado pelos use cases de persistência e consulta, garantindo
 * consistência na deduplicação.
 */
public class AudioHashService {

    private static final String ALGORITHM = "SHA-256";

    private AudioHashService() {
        // Utilitário estático — sem instância
    }

    /**
     * Gera o hash SHA-256 hexadecimal dos bytes do áudio.
     *
     * @param audioBytes conteúdo binário do arquivo de áudio
     * @return string hexadecimal de 64 caracteres
     */
    public static String generate(byte[] audioBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = md.digest(audioBytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 é garantido pela JVM (RFC 3174) — nunca ocorre em runtime
            throw new IllegalStateException("Algoritmo SHA-256 indisponível na JVM", e);
        }
    }
}