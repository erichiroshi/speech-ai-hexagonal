package com.erichiroshi.speechaihexagonal.notification.application;

import com.erichiroshi.speechaihexagonal.notification.application.port.out.NotificationPort;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * <p>O Spring injeta todos os beans que implementam {@link NotificationPort}.
 * O use case monta um mapa {@code canal → adapter} na construção e delega
 * para o adapter correto em tempo de execução.
 *
 */
@Slf4j
@Component
public class NotificationFactory {

    private final Map<NotificationChannel, NotificationPort> adapters;

    public NotificationFactory(List<NotificationPort> ports) {
        this.adapters = ports
                .stream()
                .collect(
                        Collectors.toMap(
                                NotificationPort::channel,
                                Function.identity()));
    }

    public NotificationPort get(NotificationChannel channel) {
        NotificationPort notificationAdapter = adapters.get(channel);
        if (notificationAdapter == null) {
            log.warn("Canal de notificação sem adapter registrado | channel={}", channel);
            throw new IllegalArgumentException("Canal de notificação sem adapter registrado: " + channel);
        }

        return notificationAdapter;
    }
}