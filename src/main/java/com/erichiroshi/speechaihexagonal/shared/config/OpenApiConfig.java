package com.erichiroshi.speechaihexagonal.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração global da especificação OpenAPI 3.
 *
 * <p>Centraliza metadados da API, agrupamentos por domínio e preparação
 * para autenticação JWT via Bearer Token (fase futura).
 *
 * <p>Endpoints expostos:
 * <ul>
 *   <li>Swagger UI: {@code /swagger-ui.html}</li>
 *   <li>Spec JSON:  {@code /v3/api-docs}</li>
 *   <li>Spec YAML:  {@code /v3/api-docs.yaml}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .components(new Components()
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Speech AI Hexagonal API")
                .description("""
                        API REST de transcrição de áudio e análise por LLM.
                        
                        **Funcionalidades:**
                        - Transcrição de áudio via Speaches (Whisper local) ou OpenAI Whisper-1
                        - Deduplicação automática por hash SHA-256 (Redis + PostgreSQL)
                        - Geração de resumos via Ollama (LLM local)
                        - Notificações multicanal: Email, SMS e WhatsApp
                        
                        **Arquitetura:** Hexagonal (Ports & Adapters) com 3 bounded contexts independentes.
                        """)
                .version("v12.1.0")
                .contact(new Contact()
                        .name("Eric Hiroshi")
                        .url("https://www.linkedin.com/in/eric-hiroshi/")
                        .email("erichiroshi@hotmail.com"))
                .license(new License()
                        .name("MIT")
                        .url("https://opensource.org/licenses/MIT"));
    }
}