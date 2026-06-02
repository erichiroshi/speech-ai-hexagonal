package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel;

import com.erichiroshi.speechaihexagonal.notification.application.port.out.NotificationPort;
import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NoOpNotificationAdapter implements NotificationPort {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.NO_OP;
    }

    @Override
    public void send(Notification notification) {
        log.debug("Notificação (no-op) | channel={} | notificationId={} | recipient='{}' | text='{}'",
                notification.getChannel(), notification.getId().id(), notification.getRecipient(), notification.getMessage());
    }
}
