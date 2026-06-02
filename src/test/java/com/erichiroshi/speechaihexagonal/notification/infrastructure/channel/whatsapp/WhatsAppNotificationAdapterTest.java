package com.erichiroshi.speechaihexagonal.notification.infrastructure.channel.whatsapp;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class WhatsAppNotificationAdapterTest {

    private static final String PROVIDER_URL = "https://whatsapp.com";
    private static final String API_KEY = "secret_token";
    @Mock
    private WhatsAppProperties waProperties;
    private MockRestServiceServer mockServer;
    private WhatsAppNotificationAdapter adapter;
    private Notification notification;

    private static Notification notification() {
        return Notification.create(
                "5511999999999", "Aviso Urgente", "Sua transcrição foi concluída.",
                NotificationChannel.WHATSAPP, NotificationType.TRANSCRIPTION_COMPLETED, "a".repeat(64));
    }

    @BeforeEach
    void setUp() {
        notification = notification();

        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();

        adapter = new WhatsAppNotificationAdapter(waProperties, restClient);

        when(waProperties.providerUrl()).thenReturn(PROVIDER_URL);
        when(waProperties.apiKey()).thenReturn(API_KEY);
    }

    @Test
    @DisplayName("Deve retornar o canal de notificação WHATSAPP correto")
    void shouldReturnCorrectNotificationChannel() {
        var result = adapter.channel();
        assertEquals(NotificationChannel.WHATSAPP, result);
    }

    @Test
    @DisplayName("Deve enviar WhatsApp com sucesso e payload formatado corretamente")
    void shouldSendWhatsAppWithSuccess() {
        String expectedText = "*Aviso Urgente*%n%nSua transcrição foi concluída.".formatted();

        // Configura a expectativa do servidor HTTP mockado do Spring
        mockServer.expect(requestTo(PROVIDER_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andExpect(jsonPath("$.messaging_product").value("whatsapp"))
                .andExpect(jsonPath("$.to").value("5511999999999"))
                .andExpect(jsonPath("$.type").value("text"))
                .andExpect(jsonPath("$.text.body").value(expectedText))
                .andRespond(withSuccess());

        // Executa o método sob teste
        adapter.send(notification);

        // Garante que todas as chamadas HTTP configuradas acima realmente aconteceram
        mockServer.verify();
    }

    @Test
    @DisplayName("Deve lançar NotificationException se a requisição HTTP falhar")
    void shouldThrowNotificationExceptionWhenHttpCallFails() {
        // Configura o servidor mockado para simular um erro HTTP (ex: Timeout ou Bad Request)
        mockServer.expect(requestTo(PROVIDER_URL))
                .andRespond(withException(new IOException("Connection Timeout")));

        // Executa e valida o encapsulamento da Exception de domínio
        var exception = assertThrows(NotificationException.class, () -> adapter.send(notification));

        assertAll(
                () -> assertEquals("WHATSAPP", exception.getChannel()),
                () -> assertEquals(notification.getRecipient(), exception.getRecipient()),
                () -> mockServer.verify()
        );
    }
}
