package com.erichiroshi.speechaihexagonal.notification.domain.exception;

/**
 * Exceção lançada quando o envio de uma notificação falha.
 */
public class NotificationException extends RuntimeException {

    private final String channel;
    private final String recipient;

    public NotificationException(String channel, String recipient, Throwable cause) {
        super("Falha ao enviar notificação via %s para %s".formatted(channel, recipient), cause);
        this.channel   = channel;
        this.recipient = recipient;
    }

    public String getChannel()   { return channel; }
    public String getRecipient() { return recipient; }
}
