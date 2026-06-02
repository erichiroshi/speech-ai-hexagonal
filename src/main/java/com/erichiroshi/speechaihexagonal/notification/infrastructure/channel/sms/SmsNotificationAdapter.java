package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel.sms;

import com.erichiroshi.speechaihexagonal.notification.application.port.out.NotificationPort;
import com.erichiroshi.speechaihexagonal.notification.domain.exception.NotificationException;
import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Output Adapter — envia notificações por SMS via API REST (Twilio-compatible).
 *
 * <p>Substituível por AWS SNS ou qualquer provider SMS
 * sem alterar {@link NotificationPort} ou o use case.
 *
 * <p>Configuração via:
 * <pre>
 * app:
 *   notification:
 *     sms:
 *       provider-url: ${SMS_PROVIDER_URL}
 *       api-key: ${SMS_API_KEY}
 *       from-number: ${SMS_FROM_NUMBER}
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(SmsProperties.class)
public class SmsNotificationAdapter implements NotificationPort {

    private final RestClient    restClient;
    private final SmsProperties smsProperties;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(Notification notification) {
        log.info("Enviando SMS | to={}", notification.getRecipient());

        try {
            String smsText = buildSmsText(notification);

            restClient.post()
                    .uri(smsProperties.providerUrl())
                    .header("Authorization", "Bearer " + smsProperties.apiKey())
                    .body(Map.of(
                            "to",   notification.getRecipient(),
                            "from", smsProperties.fromNumber(),
                            "body", smsText))
                    .retrieve()
                    .toBodilessEntity();

            log.info("SMS enviado | to={}", notification.getRecipient());
        } catch (Exception ex) {
            log.error("Falha ao enviar SMS | to={}", notification.getRecipient(), ex);
            throw new NotificationException("SMS", notification.getRecipient(), ex);
        }
    }

    private String buildSmsText(Notification notification) {
        // SMS tem limite de 160 chars — truncar mensagem
        String body = notification.getMessage();
        int maxBody = 140 - notification.getSubject().length();
        if (body.length() > maxBody) {
            body = body.substring(0, maxBody) + "...";
        }
        return "[%s] %s".formatted(notification.getSubject(), body);
    }
}
