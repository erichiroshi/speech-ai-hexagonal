package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel.email;

import com.erichiroshi.speechaihexagonal.notification.domain.exception.NotificationException;
import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationAdapterTest {

    private static final String FROM_EMAIL = "no-reply@speechai.com";
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private EmailProperties emailProperties;
    @InjectMocks
    private EmailNotificationAdapter adapter;
    private Notification notification;

    private static Notification notification() {
        return Notification.create(
                "user@example.com", "Assunto de Teste", "Conteúdo do e-mail de teste",
                NotificationChannel.EMAIL, NotificationType.TRANSCRIPTION_COMPLETED, "a".repeat(64));
    }

    @BeforeEach
    void setUp() {
        notification = notification();
    }

    @Test
    @DisplayName("Deve retornar o canal de notificação EMAIL correto")
    void shouldReturnCorrectNotificationChannel() {
        var result = adapter.channel();
        assertEquals(NotificationChannel.EMAIL, result);
    }

    @Test
    @DisplayName("Deve enviar e-mail com sucesso mapeando os campos corretamente")
    void shouldSendEmailWithSuccess() {
        // Given
        when(emailProperties.from()).thenReturn(FROM_EMAIL);
        var argumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        adapter.send(notification);

        // Then
        verify(mailSender).send(argumentCaptor.capture());
        var sentMessage = argumentCaptor.getValue();

        assertAll(
                () -> assertEquals(FROM_EMAIL, sentMessage.getFrom()),
                () -> assertEquals(notification.getRecipient(), Objects.requireNonNull(sentMessage.getTo())[0]),
                () -> assertEquals(notification.getSubject(), sentMessage.getSubject()),
                () -> assertEquals(notification.getMessage(), sentMessage.getText())
        );
    }

    @Test
    @DisplayName("Deve lançar NotificationException quando o JavaMailSender falhar")
    void shouldThrowNotificationExceptionWhenMailSenderFails() {
        // Given
        when(emailProperties.from()).thenReturn(FROM_EMAIL);
        var errorMessage = "Falha ao enviar notificação via EMAIL para user@example.com";
        doThrow(new MailSendException(errorMessage)).when(mailSender).send(annotationMessage());

        // When & Then
        var exception = assertThrows(NotificationException.class, () -> adapter.send(notification));

        assertAll(
                () -> assertEquals("EMAIL", exception.getChannel()), // ajuste conforme os métodos getter da sua NotificationException
                () -> assertEquals(notification.getRecipient(), exception.getRecipient()),
                () -> assertEquals(errorMessage, exception.getMessage())
        );
    }

    private SimpleMailMessage annotationMessage() {
        return org.mockito.ArgumentMatchers.any(SimpleMailMessage.class);
    }
}
