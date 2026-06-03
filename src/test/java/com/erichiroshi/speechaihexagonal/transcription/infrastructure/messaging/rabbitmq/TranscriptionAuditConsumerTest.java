package com.erichiroshi.speechaihexagonal.transcription.infrastructure.messaging.rabbitmq;

import com.erichiroshi.speechaihexagonal.transcription.domain.event.TranscriptionCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers // Habilita o ciclo de vida automático do Testcontainers
@ExtendWith(OutputCaptureExtension.class)
class TranscriptionAuditConsumerIT {

    @Container
    static RabbitMQContainer rabbitMQ =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management-alpine"));
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQ::getAdminPassword);
    }

    @Test
    void deveConsumirEventoDeTranscricaoComSucessoEGerarLogDeAuditoria(CapturedOutput output) {
        var event = TranscriptionCompletedEvent.of(
                "abc123hash",
                "Texto transcrito com sucesso da inteligência artificial.",
                "audio_aula_01.mp3",
                1024L,
                false
        );

        rabbitTemplate.convertAndSend(RabbitMqTranscriptionConfig.TRANSCRIPTION_QUEUE, event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        assertThat(output.getOut())
                                .contains("[AUDIT] TranscriptionCompleted")
                                .contains("audioHash=abc123hash")
                                .contains("fromCache=false")
                                .contains("fileName=audio_aula_01.mp3"));
    }

    @Test
    void deveTratarTextoNuloNoEventoSemLancarExcecao(CapturedOutput output) {

        var event = TranscriptionCompletedEvent.of(
                "xyz987hash",
                null,
                "audio_vazio.mp3",
                1024L,
                true
        );

        rabbitTemplate.convertAndSend(RabbitMqTranscriptionConfig.TRANSCRIPTION_QUEUE, event);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        assertThat(output.getOut())
                                .contains("[AUDIT] TranscriptionCompleted")
                                .contains("audioHash=xyz987hash")
                                .contains("fromCache=true")
                                .contains("chars=0")
                );
    }
}
