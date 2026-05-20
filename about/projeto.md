# speech-ai-hexagonal — Visão Geral do Projeto

## O que é

API REST de transcrição de áudio construída com **arquitetura hexagonal** (Ports & Adapters).
O projeto nasce como portfólio técnico com o objetivo de demonstrar domínio de padrões de design,
desacoplamento real entre camadas e evolução incremental com qualidade de produção.

## Por que arquitetura hexagonal?

A arquitetura hexagonal resolve um problema concreto: **acoplamento com frameworks e infraestrutura**.

Em sistemas convencionais, trocar o banco de dados, o motor de IA ou o broker de mensagens
exige modificar lógica de negócio. Na arquitetura hexagonal, o domínio é isolado por **portas**
(interfaces Java puras) e a infraestrutura implementa essas portas como **adapters** plugáveis.

```
Resultado prático:
- Speaches (local) → OpenAI Whisper (cloud): troca o adapter, o domínio não muda
- InMemory (dev) → Redis (prod): troca o adapter, o use case não muda
- WebClient → qualquer HTTP client: troca o adapter, nada muda
```

## Bounded Context: `transcription/`

O projeto está organizado por **bounded context** dentro de `com.erichiroshi.speechaihexagonal`:

```
transcription/
├── domain/          → entidades e exceções — zero Spring
├── application/     → use case + portas — apenas interfaces + domain
└── infrastructure/  → adapters concretos + config Spring
```

**Regra de ouro:** `domain` não importa `application`. `application` não importa `infrastructure`.

## Estrutura de camadas

```
domain/           → Transcription, AudioValidationException, SpeechToTextException
                    Zero dependência de framework. Testável de forma isolada.

application/      → TranscribeAudioUseCase (porta de entrada)
                    SpeechToTextPort, TranscriptionCachePort (portas de saída)
                    TranscriptionService (use case — orquestra o fluxo)

infrastructure/   → SpeachesAdapter, InMemoryCacheAdapter (adapters de saída)
                    TranscriptionController, GlobalExceptionHandler (adapter de entrada)
                    WebClientConfig, AppProperties (config Spring)
```

## Portas e Adapters

| Porta | Tipo | Adapter Fase 1 | Adapter Futuro |
|-------|------|----------------|----------------|
| `TranscribeAudioPort` | Entrada (driving) | `TranscribeAudioUseCase` | — |
| `SpeechToTextPort` | Saída (driven) | `SpeachesAdapter` | `OpenAiSpeechAdapter` (Fase 5) |
| `TranscriptionCachePort` | Saída (driven) | — | `RedisCacheAdapter` (Fase 2) |
| `EventPublisherPort` | Saída (driven) | — | `RabbitMqEventPublisher` (Fase 7) |
| `LanguageModelPort` | Saída (driven) | — | `OllamaLanguageModelAdapter` (Fase 6) |

## Roadmap de fases

| Fase | Descrição | Versão | Status |
|------|-----------|--------|--------|
| 1 | Base hexagonal + transcrição local (Speaches/Whisper) | v1.0.0 | ✅ Concluída |
| 2 | Cache Redis com SHA-256 | v2.x | 🔜 Planejada |
| 3 | Observabilidade (Prometheus, Grafana, Zipkin, Logs JSON) | v3.x | 🔜 Planejada |
| 4 | Resiliência com Resilience4j | v4.x | 🔜 Planejada |
| 5 | Spring AI + OpenAI Whisper (segunda porta de saída) | v5.x | 🔜 Planejada |
| 6 | Spring AI + Ollama — resumo por LLM local (≤1B) | v6.x | 🔜 Planejada |
| 7 | RabbitMQ — mensageria assíncrona | v7.x | 🔜 Planejada |
| 8 | CI/CD: GitHub Actions, SonarCloud, Codecov, Docker Hub | v8.x | 🔜 Planejada |

## Stack

| Tecnologia | Versão               | Papel                                |
|-----------|----------------------|--------------------------------------|
| Java | 25                   | Linguagem                            |
| Spring Boot | 4.0.6                | Framework                            |
| Spring MVC (webmvc) | —                    | Camada HTTP                          |
| RestClient | Spring 6.1+          | Cliente HTTP síncrono                |
| Speaches | latest-cuda          | Servidor Whisper local (transcrição) |
| Whisper | faster-whisper-small | Modelo de transcrição                |
| Lombok | —                    | Redução de boilerplate               |
| MapStruct | 1.7.0.Beta1          | Mapeamento entre camadas             |
| Springdoc OpenAPI | 3.0.1                | Swagger UI                           |
| Redis | 7                    | Cache (Fase 2)                       |
| RabbitMQ | 3                    | Mensageria (Fase 7)                  |
| Prometheus | —                    | Métricas (Fase 3)                    |
| Grafana | —                    | Dashboards (Fase 3)                  |
| Zipkin | —                    | Tracing distribuído (Fase 3)         |
| Testcontainers | 1.21.x               | Testes de integração                 |
| MockWebServer | 4.12.x               | Testes de adapters HTTP              |
| JaCoCo | —                    | Cobertura de código (≥60%)               |
| SonarCloud | —                    | Qualidade de código                  |
| GitHub Actions | —                    | CI/CD                                |
| Docker Hub | —                    | Registro de imagens                  |

## Autor

Eric Hiroshi — [github.com/erichiroshi](https://github.com/erichiroshi) · [LinkedIn](https://linkedin.com/in/eric-hiroshi)
