package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel.whatsapp;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades do canal WhatsApp.
 */
@ConfigurationProperties(prefix = "app.notification.whatsapp")
public record WhatsAppProperties(
        String providerUrl,
        String apiKey,
        String fromNumber,
        String phoneNumberId
) {}
