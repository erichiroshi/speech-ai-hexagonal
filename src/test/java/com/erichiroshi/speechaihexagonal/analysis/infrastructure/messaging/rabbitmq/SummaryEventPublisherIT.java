package com.erichiroshi.speechaihexagonal.analysis.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.analysis.domain.event.SummaryCompletedEvent;
import com.erichiroshi.speechaihexagonal.shared.rabbitmq.RabbitMqConfig;
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

@Testcontainers
@SpringBootTest(
        classes = {
                RabbitAutoConfiguration.class,
                RabbitMqAnalysisConfig.class,
                RabbitMqSummaryEventPublisher.class,
                RabbitMqConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.main.allow-bean-definition-overriding=true"
)
@DisplayName("SummaryEventPublisher — integração RabbitMQ")
class SummaryEventPublisherIT {

    private static final String HASH = "s".repeat(64);
    private static final String MODEL = "llama3.2:1b";


    @Container
    static RabbitMQContainer rabbitMQ =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management-alpine"));

    @Autowired
    private RabbitMqSummaryEventPublisher publisher;

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
    @DisplayName("deve publicar e consumir SummaryCompletedEvent com sucesso")
    void devePublicarEConsumirEvento() {
        // Monte o construtor ou factory do seu SummaryCompletedEvent real aqui
        SummaryCompletedEvent event = SummaryCompletedEvent.of(
                HASH,
                "Texto resumido de teste.",
                MODEL,
                false
        );

        publisher.publish(event);

        AtomicReference<SummaryCompletedEvent> received = new AtomicReference<>();

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> {
                    // Consome da fila correta configurada para o Summary
                    SummaryCompletedEvent msg = (SummaryCompletedEvent)
                            rabbitTemplate.receiveAndConvert(RabbitMqAnalysisConfig.SUMMARY_QUEUE);
                    assertThat(msg).isNotNull();
                    received.set(msg);
                });
    }

    @Test
    @DisplayName("deve criar filas normais e DLQ ao inicializar o contexto")
    void deveCriarFilaEDLQ() {
        Boolean resultado = rabbitTemplate.execute(channel -> {
            try {
                // Valida se as filas declaradas na sua classe de configuração existem
                channel.queueDeclarePassive(RabbitMqAnalysisConfig.SUMMARY_QUEUE);
                channel.queueDeclarePassive(RabbitMqAnalysisConfig.SUMMARY_DLQ);
                return true;
            } catch (Exception _) {
                return false;
            }
        });

        assertThat(resultado).isTrue();
    }
}
