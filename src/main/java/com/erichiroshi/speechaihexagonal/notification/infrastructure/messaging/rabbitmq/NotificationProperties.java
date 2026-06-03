package com.erichiroshi.speechaihexagonal.notification.infrastructure.messaging.rabbitmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuração do contexto de notificação.
 *
 * <pre>
 * app:
 *   notification:
 *     no-op-recipient: ${NOTIFICATION_NO_OP:noreply@example.com}
 *     email-recipient: ${NOTIFICATION_EMAIL}
 *     sms-recipient: ${NOTIFICATION_SMS_NUMBER}
 *     whatsapp-recipient: ${NOTIFICATION_WHATSAPP_NUMBER}
 *     channels:
 *       email: false
 *       sms: false
 *       whatsapp: false
 *       noOp: true
 * </pre>
 */
@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(
        String emailRecipient,
        String smsRecipient,
        String whatsappRecipient,
        String noOpRecipient,
        Channels channels
) {
    public record Channels(
            boolean email,
            boolean sms,
            boolean whatsapp,
            boolean noOp
    ) {
        public Channels {
            noOp = true;
        }
    }
}
