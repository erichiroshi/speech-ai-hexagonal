package com.erichiroshi.speechaihexagonal.notification.application.port.in;

import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;

/**
 * Driver Port — expõe o caso de uso de envio de notificação.
 * O consumer RabbitMQ invoca esta porta; não conhece os canais internos.
 */
public interface SendNotificationPort {

    void execute(Notification notification);
}
