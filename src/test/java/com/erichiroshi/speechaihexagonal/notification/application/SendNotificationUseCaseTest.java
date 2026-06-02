package com.erichiroshi.speechaihexagonal.notification.application;

import com.erichiroshi.speechaihexagonal.notification.application.port.out.NotificationPort;
import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendNotificationUseCase")
class SendNotificationUseCaseTest {

    private SendNotificationUseCase useCase;

    // 1. Declare apenas os mocks específicos de cada canal
    @Mock
    private NotificationPort emailAdapterMock;
    @Mock
    private NotificationPort smsAdapterMock;
    @Mock
    private NotificationPort whatsAppAdapterMock;

    private static Notification notification(NotificationChannel channel) {
        return Notification.create(
                "dest@test.com", "Título", "Mensagem de teste",
                channel, NotificationType.TRANSCRIPTION_COMPLETED, "a".repeat(64));
    }

    @BeforeEach
    void setUp() {
        // 2. Configure o comportamento que a factory exige (NotificationPort::channel)
        when(emailAdapterMock.channel()).thenReturn(NotificationChannel.EMAIL);
        when(smsAdapterMock.channel()).thenReturn(NotificationChannel.SMS);
        when(whatsAppAdapterMock.channel()).thenReturn(NotificationChannel.WHATSAPP);

        // 3. Instancie a Factory real passando a lista de mocks
        List<NotificationPort> ports = List.of(emailAdapterMock, smsAdapterMock, whatsAppAdapterMock);
        NotificationFactory notificationFactory = new NotificationFactory(ports);

        // 4. Instancie o seu Use Case injetando a factory real configurada
        this.useCase = new SendNotificationUseCase(notificationFactory);
    }

    @Test
    @DisplayName("deve rotear para EmailPort quando canal é EMAIL")
    void deveRotearpraEmail() {
        Notification notification = notification(NotificationChannel.EMAIL);

        useCase.execute(notification);

        verify(emailAdapterMock, times(1)).send(notification);
        verifyNoMoreInteractions(smsAdapterMock, whatsAppAdapterMock);
    }

    @Test
    @DisplayName("deve rotear para SmsPort quando canal é SMS")
    void deveRotearpraSmS() {
        Notification notification = notification(NotificationChannel.SMS);

        useCase.execute(notification);

        verify(smsAdapterMock, times(1)).send(notification);
        verifyNoMoreInteractions(emailAdapterMock, whatsAppAdapterMock);
    }

    @Test
    @DisplayName("deve rotear para WhatsAppPort quando canal é WHATSAPP")
    void deveRotearpraWhatsApp() {
        Notification notification = notification(NotificationChannel.WHATSAPP);

        useCase.execute(notification);

        verify(whatsAppAdapterMock, times(1)).send(notification);
        verifyNoMoreInteractions(emailAdapterMock, smsAdapterMock);
    }
}
