package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel.sms;

import com.erichiroshi.speechaihexagonal.notification.domain.exception.NotificationException;
import com.erichiroshi.speechaihexagonal.notification.domain.model.Notification;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationChannel;
import com.erichiroshi.speechaihexagonal.notification.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class SmsNotificationAdapterTest {

    private static final String PROVIDER_URL = "https://sms-provider.com";
    private static final String API_KEY = "sms_secret_token";
    private static final String FROM_NUMBER = "+123456789";
    @Mock
    private SmsProperties smsProperties;
    private MockRestServiceServer mockServer;
    private SmsNotificationAdapter adapter;
    private Notification notification;

    private static Notification notification(String text) {
        return Notification.create(
                "+5511999999999", "SpeechAI", text,
                NotificationChannel.SMS, NotificationType.TRANSCRIPTION_COMPLETED, "a".repeat(64));
    }

    @BeforeEach
    void setUp() {
        var restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        var restClient = restClientBuilder.build();

        adapter = new SmsNotificationAdapter(restClient, smsProperties);
    }

    @Test
    @DisplayName("Deve retornar o canal de notificação SMS correto")
    void shouldReturnCorrectNotificationChannel() {
        var result = adapter.channel();
        assertEquals(NotificationChannel.SMS, result);
    }

    @Test
    @DisplayName("Deve enviar SMS com sucesso quando o texto for curto")
    void shouldSendSmsWithShortTextSuccessfully() {
        // Given
        stubSmsProperties();
        notification = notification("Sua transcrição terminou.");
        String expectedSmsText = "[SpeechAI] Sua transcrição terminou.";

        mockServer.expect(requestTo(PROVIDER_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andExpect(jsonPath("$.to").value("+5511999999999"))
                .andExpect(jsonPath("$.from").value(FROM_NUMBER))
                .andExpect(jsonPath("$.body").value(expectedSmsText))
                .andRespond(withSuccess());

        // When
        adapter.send(notification);

        // Then
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve truncar a mensagem adicionando reticências se exceder o limite de caracteres")
    void shouldTruncateSmsBodyWhenMessageIsTooLong() {
        // Given
        stubSmsProperties();
        String longMessage = "A".repeat(150);
        notification = notification(longMessage);

        String expectedTruncatedBody = "A".repeat(132) + "...";
        String expectedSmsText = "[SpeechAI] " + expectedTruncatedBody;

        mockServer.expect(requestTo(PROVIDER_URL))
                .andExpect(jsonPath("$.body").value(expectedSmsText))
                .andRespond(withSuccess());

        // When
        adapter.send(notification);

        // Then
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve lançar NotificationException se o provedor de SMS falhar")
    void shouldThrowNotificationExceptionWhenSmsProviderFails() {
        // Given
        stubSmsProperties();
        notification = notification("Mensagem de erro");

        mockServer.expect(requestTo(PROVIDER_URL))
                .andRespond(withException(new IOException("API Gateway Error")));

        // When & Then
        var exception = assertThrows(NotificationException.class, () -> adapter.send(notification));

        assertAll(
                () -> assertEquals("SMS", exception.getChannel()),
                () -> assertEquals(notification.getRecipient(), exception.getRecipient()),
                () -> mockServer.verify()
        );
    }

    // Helper method para isolar os stubs apenas nos testes que usam as propriedades
    private void stubSmsProperties() {
        when(smsProperties.providerUrl()).thenReturn(PROVIDER_URL);
        when(smsProperties.apiKey()).thenReturn(API_KEY);
        when(smsProperties.fromNumber()).thenReturn(FROM_NUMBER);
    }
}

