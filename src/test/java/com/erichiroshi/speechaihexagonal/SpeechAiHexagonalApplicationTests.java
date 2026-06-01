package com.erichiroshi.speechaihexagonal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
        classes = {
                RabbitAutoConfiguration.class,
        }
)
@ActiveProfiles("test")
@DisplayName("SpeechAiHexagonalApplication")
class SpeechAiHexagonalApplicationTests {

    @Test
    @DisplayName("deve carregar o contexto Spring sem erros")
    void contextLoads() {
    }
}