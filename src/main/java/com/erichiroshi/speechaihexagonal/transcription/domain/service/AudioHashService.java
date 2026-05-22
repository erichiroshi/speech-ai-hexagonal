package com.erichiroshi.speechaihexagonal.transcription.domain.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class AudioHashService {

    private static final String ALGORITHM = "SHA-256";

    private AudioHashService() {
        // Utilitário estático — sem instância
    }

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