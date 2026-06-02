package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades do canal de e-mail.
 */
@ConfigurationProperties(prefix = "app.notification.email")
public record EmailProperties(
        String from
) {

    public String from() {
        return from != null ? from : "noreply@speech-ai.com";
    }
}
