package com.erichiroshi.speechaihexagonal.notification.application;

import com.erichiroshi.speechaihexagonal.notification.application.port.in.SendNotificationPort;
import com.erichiroshi.speechaihexagonal.notification.application.port.out.NotificationPort;
import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Use Case de envio de notificações multicanal.
 *
 * <p>Open/Closed Principle em ação: aberto para extensão (novos canais),
 * basta criar um novo adapter que implemente {@link NotificationPort},
 * fechado para modificação (use case não muda).
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SendNotificationUseCase implements SendNotificationPort {

    private final NotificationFactory notificationFactory;

    @Override
    public void execute(Notification notification) {

        log.info("Enviando notificação | channel={} | type={} | recipient={}",
                notification.getChannel(), notification.getType(), notification.getRecipient());

        try {
            notificationFactory.get(notification.getChannel()).send(notification);

            log.info("Notificação enviada | channel={} | notificationId={} | audioHash={}",
                    notification.getChannel(), notification.getId(), notification.getAudioHash());

        } catch (Exception ex) {
            log.error("Falha ao enviar notificação | channel={} | notificationId={} | error={}",
                    notification.getChannel(), notification.getId(), ex.getMessage());
            throw ex;
        }
    }
}
