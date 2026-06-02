package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel;

import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NoOpNotificationAdapterTest {

    private NoOpNotificationAdapter adapter;

    private static Notification notification(NotificationChannel channel) {
        return Notification.create(
                "dest@test.com", "Título", "Mensagem de teste",
                channel, NotificationType.TRANSCRIPTION_COMPLETED, "a".repeat(64));
    }

    @BeforeEach
    void setUp() {
        adapter = new NoOpNotificationAdapter();
    }

    @Test
    @DisplayName("Deve retornar o canal de notificação NO_OP correto")
    void shouldReturnCorrectNotificationChannel() {
        NotificationChannel result = adapter.channel();

        assertEquals(NotificationChannel.NO_OP, result);
    }

    @Test
    @DisplayName("Deve executar o envio sem lançar exceções")
    void shouldSendNotificationWithoutThrowingException() {
        var notification = createDummyNotification();

        assertDoesNotThrow(() -> adapter.send(notification));
    }

    private Notification createDummyNotification() {
        return notification(NotificationChannel.NO_OP);
    }
}