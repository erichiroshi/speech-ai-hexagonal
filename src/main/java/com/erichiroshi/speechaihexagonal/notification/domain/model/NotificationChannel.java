package com.erichiroshi.speechaihexagonal.notification.domain.model;

/**
 * Value Object que representa o canal de envio de uma notificação.
 * Extensível — novos canais não afetam domínio ou use cases existentes.
 */
public enum NotificationChannel {
    EMAIL,
    SMS,
    WHATSAPP,
    NO_OP
}
