package com.erichiroshi.speechaihexagonal.notification.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.analysis.domain.event.SummaryCompletedEvent;
import com.erichiroshi.speechaihexagonal.notification.application.port.in.SendNotificationPort;
import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationType;
import com.erichiroshi.speechaihexagonal.transcription.domain.event.TranscriptionCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    private static final String HASH = "audio-123-hash";
    private static final String NO_OP_REC = "no-op-target";
    private static final String EMAIL_REC = "user@speechai.com";
    private static final String SMS_REC = "+5511999999999";
    private static final String WA_REC = "5511988888888";
    @Mock
    private SendNotificationPort sendNotificationPort;
    @Mock
    private NotificationProperties properties;
    @InjectMocks
    private NotificationEventConsumer consumer;
    private NotificationProperties.Channels channelsMock;

    private static TranscriptionCompletedEvent transcriptionCompletedEvent(String text, String fileName, boolean fromCache) {
        return TranscriptionCompletedEvent.of(
                HASH, text, fileName, "text".length(), fromCache);
    }

    private static SummaryCompletedEvent summaryCompletedEvent(String text, String model, boolean fromCache) {
        return SummaryCompletedEvent.of(
                HASH, text, model, fromCache);
    }

    @BeforeEach
    void setUp() {
        channelsMock = mock(NotificationProperties.Channels.class);
        when(properties.channels()).thenReturn(channelsMock);
    }

    @Test
    @DisplayName("Deve processar evento de transcrição e enviar para múltiplos canais habilitados")
    void shouldProcessTranscriptionEventAndSendToAllEnabledChannels() {
        // Given
        stubAllChannels();
        stubAllRecipients();

        var event = transcriptionCompletedEvent(
                "Texto longo da transcrição.", "aula_java.mp3", false
        );

        var captor = ArgumentCaptor.forClass(Notification.class);

        // When
        consumer.onTranscriptionCompleted(event);

        // Then
        verify(sendNotificationPort, times(4)).execute(captor.capture());
        List<Notification> sentNotifications = captor.getAllValues();

        assertAll(
                () -> assertEquals(4, sentNotifications.size()),
                () -> assertNotificationFields(sentNotifications.getFirst(), NO_OP_REC, NotificationChannel.NO_OP, NotificationType.TRANSCRIPTION_COMPLETED),
                () -> assertNotificationFields(sentNotifications.get(1), EMAIL_REC, NotificationChannel.EMAIL, NotificationType.TRANSCRIPTION_COMPLETED),
                () -> assertNotificationFields(sentNotifications.get(2), SMS_REC, NotificationChannel.SMS, NotificationType.TRANSCRIPTION_COMPLETED),
                () -> assertNotificationFields(sentNotifications.get(3), WA_REC, NotificationChannel.WHATSAPP, NotificationType.TRANSCRIPTION_COMPLETED)
        );

        assertTrue(sentNotifications.get(0).getMessage().contains("Arquivo: aula_java.mp3"));
    }

    @Test
    @DisplayName("Deve truncar o texto da transcrição na mensagem se ele passar de 200 caracteres")
    void shouldTruncateTranscriptionTextWhenExceedingLimit() {
        // Given
        when(channelsMock.email()).thenReturn(true);
        when(properties.emailRecipient()).thenReturn(EMAIL_REC);

        String longText = "A".repeat(250);
        var event = transcriptionCompletedEvent(longText, "test.mp3", true);
        var captor = ArgumentCaptor.forClass(Notification.class);

        // When
        consumer.onTranscriptionCompleted(event);

        // Then
        verify(sendNotificationPort).execute(captor.capture());
        String expectedTruncatedText = "A".repeat(200) + "...";
        assertTrue(captor.getValue().getMessage().contains("Prévia: " + expectedTruncatedText));
    }

    @Test
    @DisplayName("Deve processar evento de resumo e enviar apenas para os canais ativos")
    void shouldProcessSummaryEventAndSendOnlyToActiveChannels() {
        // Given
        when(channelsMock.noOp()).thenReturn(false);
        when(channelsMock.email()).thenReturn(true);
        when(channelsMock.sms()).thenReturn(false);
        when(channelsMock.whatsapp()).thenReturn(true);

        when(properties.emailRecipient()).thenReturn(EMAIL_REC);
        when(properties.whatsappRecipient()).thenReturn(WA_REC);

        var event = summaryCompletedEvent("Este é o resumo.", "gpt-4o", false);
        var captor = ArgumentCaptor.forClass(Notification.class);

        // When
        consumer.onSummaryCompleted(event);

        // Then
        verify(sendNotificationPort, times(2)).execute(captor.capture());
        List<Notification> sentNotifications = captor.getAllValues();

        assertAll(
                () -> assertEquals(2, sentNotifications.size()),
                () -> assertNotificationFields(sentNotifications.getFirst(), EMAIL_REC, NotificationChannel.EMAIL, NotificationType.SUMMARY_COMPLETED),
                () -> assertNotificationFields(sentNotifications.get(1), WA_REC, NotificationChannel.WHATSAPP, NotificationType.SUMMARY_COMPLETED)
        );

        assertTrue(sentNotifications.get(0).getMessage().contains("Modelo: gpt-4o"));
    }

    @Test
    @DisplayName("Não deve disparar notificações se os canais estiverem ativos mas os destinatários nulos ou em branco")
    void shouldNotSendNotificationWhenRecipientsAreBlank() {
        // Given
        when(channelsMock.email()).thenReturn(true);
        when(properties.emailRecipient()).thenReturn("   "); // Blank string

        var event = summaryCompletedEvent("Resumo", "llama-3", true);

        // When
        consumer.onSummaryCompleted(event);

        // Then
        verifyNoInteractions(sendNotificationPort);
    }

    // Métodos auxiliares para manter stubs organizados evitando UnnecessaryStubbingException
    private void stubAllChannels() {
        when(channelsMock.noOp()).thenReturn(true);
        when(channelsMock.email()).thenReturn(true);
        when(channelsMock.sms()).thenReturn(true);
        when(channelsMock.whatsapp()).thenReturn(true);
    }

    private void stubAllRecipients() {
        when(properties.noOpRecipient()).thenReturn(NO_OP_REC);
        when(properties.emailRecipient()).thenReturn(EMAIL_REC);
        when(properties.smsRecipient()).thenReturn(SMS_REC);
        when(properties.whatsappRecipient()).thenReturn(WA_REC);
    }

    private void assertNotificationFields(Notification notification, String recipient, NotificationChannel channel, NotificationType type) {
        assertEquals(recipient, notification.getRecipient());
        assertEquals(channel, notification.getChannel());
        assertEquals(type, notification.getType());
        assertEquals(HASH, notification.getAudioHash());
    }
}
