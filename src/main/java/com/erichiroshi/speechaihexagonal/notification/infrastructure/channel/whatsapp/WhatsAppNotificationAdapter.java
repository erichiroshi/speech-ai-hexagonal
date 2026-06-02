package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel.whatsapp;

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
 * Output Adapter — envia notificações por WhatsApp via API REST (Twilio/Meta-compatible).
 *
 * <p>Compatível com Twilio WhatsApp Business API e Meta Cloud API.
 * Substituível sem alterar {@link NotificationPort} ou o use case.
 *
 * <p>Configuração via:
 * <pre>
 * app:
 *   notification:
 *     whatsapp:
 *       provider-url: ${WA_PROVIDER_URL}
 *       api-key: ${WA_API_KEY}
 *       from-number: ${WA_FROM_NUMBER}
 *       phone-number-id: ${WA_PHONE_NUMBER_ID}
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(WhatsAppProperties.class)
public class WhatsAppNotificationAdapter implements NotificationPort {

    private final WhatsAppProperties waProperties;
    private final RestClient         restClient;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public void send(Notification notification) {
        log.info("Enviando WhatsApp | to={}", notification.getRecipient());

        try {
            // Payload compatível com Meta Cloud API e Twilio WhatsApp
            Map<String, Object> payload = Map.of(
                    "messaging_product", "whatsapp",
                    "to",   notification.getRecipient(),
                    "type", "text",
                    "text", Map.of("body", buildWaText(notification))
            );

            restClient.post()
                    .uri(waProperties.providerUrl())
                    .header("Authorization", "Bearer " + waProperties.apiKey())
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("WhatsApp enviado | to={}", notification.getRecipient());
        } catch (Exception ex) {
            log.error("Falha ao enviar WhatsApp | to={}", notification.getRecipient(), ex);
            throw new NotificationException("WHATSAPP", notification.getRecipient(), ex);
        }
    }

    private String buildWaText(Notification notification) {
        return "*%s*%n%n%s".formatted(notification.getSubject(), notification.getMessage());
    }
}
