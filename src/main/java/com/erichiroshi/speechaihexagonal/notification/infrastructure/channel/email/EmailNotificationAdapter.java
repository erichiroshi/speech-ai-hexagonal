package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel.email;

import com.erichiroshi.speechaihexagonal.notification.application.port.out.NotificationPort;
import com.erichiroshi.speechaihexagonal.notification.domain.exception.NotificationException;
import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Output Adapter — envia notificações por e-mail via Spring Mail (SMTP).
 *
 * <p>Substituível por SendGrid, Amazon SES ou Mailgun
 * sem alterar {@link NotificationPort} ou o use case.
 *
 * <p>Configuração via:
 * <pre>
 * Spring:
 *   mail:
 *     host: ${SMTP_HOST}
 *     port: ${SMTP_PORT:587}
 *     username: ${SMTP_USER}
 *     password: ${SMTP_PASS}
 *     properties:
 *       mail.smtp.auth: true
 *       mail.smtp.starttls.enable: true
 * app:
 *   notification:
 *     email-from: ${NOTIFICATION_EMAIL_FROM}
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(EmailProperties.class)
public class EmailNotificationAdapter implements NotificationPort {

    private final JavaMailSender    mailSender;
    private final EmailProperties   emailProperties;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(Notification notification) {
        log.info("Enviando e-mail | to={} | subject={}", notification.getRecipient(), notification.getSubject());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailProperties.from());
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getMessage());

            mailSender.send(message);

            log.info("E-mail enviado | to={}", notification.getRecipient());
        } catch (Exception ex) {
            log.error("Falha ao enviar e-mail | to={}", notification.getRecipient(), ex);
            throw new NotificationException("EMAIL", notification.getRecipient(), ex);
        }
    }


}
