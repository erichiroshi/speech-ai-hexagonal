package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades do canal SMS.
 */
@ConfigurationProperties(prefix = "app.notification.sms")
public record SmsProperties(
        String providerUrl,
        String apiKey,
        String fromNumber
) {}
