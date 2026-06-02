package com.erichiroshi.speechaihexagonal.notification.application.port.out;

import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;

/**
 * PORT — contrato de envio de notificação.
 *
 * <p>Cada canal tem um adapter que implementa esta interface:
 * <ul>
 *   <li>{@code EmailNotificationAdapter} </li>
 *   <li>{@code SmsNotificationAdapter} </li>
 *   <li>{@code WhatsAppNotificationAdapter} </li>
 * </ul>
 *
 * <p>O {@link com.erichiroshi.speechaihexagonal.notification.application.SendNotificationUseCase}
 * recebe uma lista de todos os adapters e delega para o correto com base
 * no {@link NotificationChannel} do request.
 */
public interface NotificationPort {

    /**
     * Envia a notificação para o destinatário.
     *
     * @param notification dados da notificação
     */
    void send(Notification notification);

    /**
     * Canal que este adapter suporta.
     * Usado pelo caso de uso para selecionar o adapter correto.
     */
    NotificationChannel channel();
}