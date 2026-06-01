package com.erichiroshi.speechaihexagonal.transcription.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.shared.rabbitmq.RabbitMqConfig;
import com.erichiroshi.speechaihexagonal.transcription.domain.event.TranscriptionCompletedEvent;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração do publisher e consumer com RabbitMQ real via Testcontainers.
 *
 * <p>Valida o fluxo completo: publicação → recepção pelo consumer de auditoria.
 */
@Testcontainers
@SpringBootTest(
        classes = {
                RabbitAutoConfiguration.class,
                RabbitMqTranscriptionConfig.class,
                RabbitMqTranscriptionEventPublisher.class,
                RabbitMqConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.main.allow-bean-definition-overriding=true"
)
@DisplayName("TranscriptionEventPublisher — integração RabbitMQ")
class TranscriptionEventPublisherIT {

    private static final String HASH = "a".repeat(64);

    @Container
    static RabbitMQContainer rabbitMQ =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management-alpine"));

    @Autowired
    private RabbitMqTranscriptionEventPublisher publisher;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQ::getAdminPassword);
    }

    @Test
    @DisplayName("deve publicar e consumir TranscriptionCompletedEvent com sucesso")
    void devePublicarEConsumirEvento() {
        TranscriptionCompletedEvent event = TranscriptionCompletedEvent.of(
                HASH, "Texto transcrito.", "audio.wav", 1024L, false);

        publisher.publish(event);

        // Receber diretamente da fila para validar payload
        AtomicReference<TranscriptionCompletedEvent> received = new AtomicReference<>();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    TranscriptionCompletedEvent msg = (TranscriptionCompletedEvent)
                            rabbitTemplate.receiveAndConvert(RabbitMqTranscriptionConfig.TRANSCRIPTION_QUEUE);
                    assertThat(msg).isNotNull();
                    received.set(msg);
                });

        assertThat(received.get().audioHash()).isEqualTo(HASH);
        assertThat(received.get().text()).isEqualTo("Texto transcrito.");
        assertThat(received.get().fileName()).isEqualTo("audio.wav");
        assertThat(received.get().fromCache()).isFalse();
    }

    @Test
    @DisplayName("deve criar fila DLQ ao inicializar o contexto")
    void deveCriarFilaDLQ() {
        Boolean resultado = rabbitTemplate.execute(channel -> {
            try {
                channel.queueDeclarePassive(RabbitMqTranscriptionConfig.TRANSCRIPTION_QUEUE);
                channel.queueDeclarePassive(RabbitMqTranscriptionConfig.TRANSCRIPTION_DLQ);
                return true;
            } catch (Exception _) {
                return false;
            }
        });

        assertThat(resultado).isTrue();
    }

}
