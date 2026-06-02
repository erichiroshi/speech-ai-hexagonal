package com.erichiroshi.speechaihexagonal.notification.domain.model;

import java.time.Instant;

public class Notification {

    private final NotificationId id;
    private final String recipient;
    private final String subject;
    private final String message;
    private final NotificationChannel channel;
    private final NotificationType type;
    private final String audioHash;
    private final Instant createdAt;

    // Construtor privado: aceita o objeto Builder encapsulado (1 parâmetro para o Sonar)
    private Notification(Builder builder) {
        if (builder.recipient == null || builder.recipient.isBlank()) {
            throw new IllegalArgumentException("Destinatário não pode ser vazio");
        }
        if (builder.message == null || builder.message.isBlank()) {
            throw new IllegalArgumentException("Mensagem não pode ser vazia");
        }

        this.id = builder.id != null ? builder.id : new NotificationId();
        this.recipient = builder.recipient;
        this.subject = builder.subject;
        this.message = builder.message;
        this.channel = builder.channel;
        this.type = builder.type;
        this.audioHash = builder.audioHash;
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
    }

    // Mantendo o método estático de conveniência que seus consumidores já utilizam
    public static Notification create(String recipient, String subject, String message,
                                      NotificationChannel channel, NotificationType type, String audioHash) {
        return Notification.builder()
                .recipient(recipient)
                .subject(subject)
                .message(message)
                .channel(channel)
                .type(type)
                .audioHash(audioHash)
                .createdAt(Instant.now())
                .build();
    }

    // Getters públicos em Java Puro
    public NotificationId getId() { return id; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getMessage() { return message; }
    public NotificationChannel getChannel() { return channel; }
    public NotificationType getType() { return type; }
    public String getAudioHash() { return audioHash; }
    public Instant getCreatedAt() { return createdAt; }

    // Ponto de entrada para o padrão Builder
    public static Builder builder() {
        return new Builder();
    }

    // Builder Pattern Manual (Java Puro)
    public static class Builder {
        private NotificationId id;
        private String recipient;
        private String subject;
        private String message;
        private NotificationChannel channel;
        private NotificationType type;
        private String audioHash;
        private Instant createdAt;

        public Builder id(NotificationId id) { this.id = id; return this; }
        public Builder recipient(String recipient) { this.recipient = recipient; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder channel(NotificationChannel channel) { this.channel = channel; return this; }
        public Builder type(NotificationType type) { this.type = type; return this; }
        public Builder audioHash(String audioHash) { this.audioHash = audioHash; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public Notification build() {
            return new Notification(this);
        }
    }
}