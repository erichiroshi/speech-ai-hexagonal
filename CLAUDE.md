# CLAUDE.md — speech-ai-hexagonal

## Visão geral

API REST de transcrição de áudio com **arquitetura hexagonal** em Java 25 + Spring Boot 4.0.6.
Motor de transcrição: Speaches (Whisper local via Docker).

## Status atual

✅ **Fase 1 concluída**: Base hexagonal + transcrição local (Speaches/Whisper) implementada.
🔜 **Próximas fases**: Cache Redis (Fase 2), Observabilidade (Fase 3), Resiliência (Fase 4), etc.

## Estrutura hexagonal

```
domain/          → entidades e exceções — zero dependência de framework
application/     → use cases (portas in/out) — apenas interfaces + domain
infrastructure/  → adapters de entrada e saída + config Spring
```

**Regra de ouro:** nunca importar classes de `infrastructure` dentro de `application` ou `domain`.

## Portas e adapters

| Porta | Interface | Adapter(s) |
|-------|-----------|------------|
| `SpeechToTextPort` | `application/port/out` | `SpeachesAdapter` (Fase 1), `OpenAiSpeechAdapter` (Fase 5) |
| `TranscriptionCachePort` | `application/port/out` | `InMemoryCacheAdapter` (Fase 1), `RedisCacheAdapter` (Fase 2) |

## Comandos principais

```bash
# Dev (Speaches via Docker + API via Gradle)
docker compose -f docker-compose.dev.yml up -d
./gradlew bootRun --args='--spring.profiles.active=dev'

# Testes
./gradlew test

# Cobertura
./gradlew test jacocoTestReport

# Prod
docker compose up -d
```

## Regras de desenvolvimento

- Commits em português seguindo Conventional Commits
- Threshold JaCoCo: 60% (sobe progressivamente)
- Cada fase/item gera: commit + about/fase-X-item-Y.md + post LinkedIn
- Docker Compose: serviços adicionados à medida que são implementados
- Nunca usar tag `latest` para imagens com versão definida
- `@MockitoBean` para dependências em `@WebMvcTest`

## Stack

Java 25 · Spring Boot 4.0.6 · Speaches/Whisper · Redis · RabbitMQ · Prometheus · Grafana · Zipkin · OTel · MockWebServer · Testcontainers · JaCoCo · SonarCloud · GitHub Actions · Docker Hub
