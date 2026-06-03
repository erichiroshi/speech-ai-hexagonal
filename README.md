<p align="center">
  <img width="25%" src="./images/logo eric hiroshi.png" alt="Eric Hiroshi Logo">
</p>

<h1 align="center">🎙️ Speech AI Hexagonal</h1>

> API REST de transcrição de áudio construída com **arquitetura hexagonal** (Ports & Adapters), Java 25 e Spring Boot 4.

[![SonarQube + Codecov](https://github.com/erichiroshi/speech-ai-hexagonal/actions/workflows/sonar.yml/badge.svg?branch=main)](https://github.com/erichiroshi/speech-ai-hexagonal/actions/workflows/sonar.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=erichiroshi_speech-ai-hexagonal&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=erichiroshi_speech-ai-hexagonal)
[![codecov](https://codecov.io/gh/erichiroshi/speech-ai-hexagonal/graph/badge.svg?token=8pOCWyVDRE)](https://codecov.io/gh/erichiroshi/speech-ai-hexagonal)

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-red?style=flat-square&logo=openjdk" alt="Java 25">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 4.0.6">
  <img src="https://img.shields.io/badge/Speaches-Whisper-4A90D9?style=flat-square" alt="Speaches Whisper">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white" alt="Docker Compose">
  <img src="https://img.shields.io/badge/Prometheus-E6522C?style=flat-square&logo=prometheus&logoColor=white" alt="Prometheus">
  <img src="https://img.shields.io/badge/Grafana-F46800?style=flat-square&logo=grafana&logoColor=white" alt="Grafana">
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white" alt="Redis">
  <img src="https://img.shields.io/badge/RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white" alt="RabbitMQ">
  <img src="https://img.shields.io/badge/Resilience4j-informational?style=flat-square" alt="Resilience4j">
  <img src="https://img.shields.io/badge/Spring%20AI-1.0-6DB33F?style=flat-square&logo=spring&logoColor=white" alt="Spring AI">
  <img src="https://img.shields.io/badge/Jacoco-70%25-brightgreen?style=flat-square" alt="Jacoco">
  <img src="https://img.shields.io/badge/SonarCloud-passing-4E9BCD?style=flat-square&logo=sonarcloud&logoColor=white" alt="SonarCloud">
  <img src="https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?style=flat-square&logo=githubactions&logoColor=white" alt="GitHub Actions">
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="MIT License">
</p>

---

## Índice

- [Índice](#índice)
- [🗺️ Roadmap](#️-roadmap)
- [🌐 Documentação](#-documentação)
- [🛠️ Stack](#️-stack)
- [🏗️ Arquitetura](#️-arquitetura)
  - [Por que hexagonal?](#por-que-hexagonal)
  - [Fluxo de transcrição](#fluxo-de-transcrição)
  - [Fluxo de notificação (eventos)](#fluxo-de-notificação-eventos)
- [⚙️ Pré-requisitos](#️-pré-requisitos)
- [🚀 Quick Start](#-quick-start)
  - [Modo desenvolvimento (API local + infra via Docker)](#modo-desenvolvimento-api-local--infra-via-docker)
  - [Modo produção (tudo via Docker)](#modo-produção-tudo-via-docker)
- [📡 Endpoints](#-endpoints)
  - [`POST /api/transcriptions`](#post-apitranscriptions)
  - [`POST /api/transcriptions/{audioHash}/analysis`](#post-apitranscriptionsaudiohashanalysis)
- [🔧 Variáveis de ambiente](#-variáveis-de-ambiente)
- [📊 Observabilidade](#-observabilidade)
  - [Métricas customizadas](#métricas-customizadas)
  - [Tracing](#tracing)
- [🧪 Testes](#-testes)
  - [Estratégia de testes](#estratégia-de-testes)
- [📁 Estrutura do projeto](#-estrutura-do-projeto)
- [⚠️ Troubleshooting](#️-troubleshooting)
- [Autor](#autor)

---

## 🗺️ Roadmap

| Fase   | Descrição                                                                                 | Status       |
|--------|-------------------------------------------------------------------------------------------|--------------|
| **1**  | Base hexagonal — transcrição local (Speaches/Whisper) · RestClient · Lombok               | ✅ `v1.7.0`  |
| **2**  | Qualidade de código com SonarQube e JaCoCo                                                | ✅ `v2.5.0`  |
| **3**  | Setup PostgreSQL + JPA + Flyway                                                            | ✅ `v3.7.0`  |
| **4**  | Cache Redis com SHA-256 — RedisCacheAdapter · Testcontainers                              | ✅ `v4.3.0`  |
| **5**  | Resiliência — Circuit Breaker · Retry · Bulkhead (Resilience4j)                           | ✅ `v5.3.0`  |
| **6**  | Observabilidade — Prometheus · Grafana · Zipkin/OTel · Logs JSON + MDC                    | ✅ `v6.5.0`  |
| **7**  | Governança arquitetural automatizada com ArchUnit                                         | ✅ `v7.1.0`  |
| **8**  | Spring AI + OpenAI Whisper — segunda porta de saída (cloud)                               | ✅ `v8.2.0`  |
| **9**  | Spring AI + Ollama — resumo por LLM local (bounded context `analysis/`)                  | ✅ `v9.3.0`  |
| **10** | RabbitMQ — eventos de domínio · DLQ · consumers de auditoria                             | ✅ `v10.2.0` |
| **11** | Notificações multicanal — Email · SMS · WhatsApp (bounded context `notification/`)        | ✅ `v11.5.0` |

---

## 🌐 Documentação

| Página | Descrição |
|--------|-----------|
| [Home](https://erichiroshi.github.io/speech-ai-hexagonal/) | Visão geral e quick start |
| [Arquitetura](https://erichiroshi.github.io/speech-ai-hexagonal/architecture.html) | C4 Model — Contexto, Containers, Componentes |
| [Qualidade](https://erichiroshi.github.io/speech-ai-hexagonal/quality.html) | Jacoco + SonarCloud + Codecov |
| [Persistência](https://erichiroshi.github.io/speech-ai-hexagonal/persistence.html) | PostgreSQL + Flyway |
| [Cache](https://erichiroshi.github.io/speech-ai-hexagonal/cache.html) | Redis distribuído |
| [Observabilidade](https://erichiroshi.github.io/speech-ai-hexagonal/observability.html) | Prometheus + Grafana + Zipkin + Logs JSON |
| [Roadmap](https://erichiroshi.github.io/speech-ai-hexagonal/roadmap.html) | Roadmap do projeto |

---

## 🛠️ Stack

| Tecnologia       | Versão         | Papel                                    |
|-----------------|----------------|------------------------------------------|
| Java             | 25             | Linguagem                                |
| Spring Boot      | 4.0.6          | Framework web + DI                       |
| Spring AI        | 2.0.0-M5       | Integração com OpenAI e Ollama           |
| Speaches         | latest-cuda    | Servidor Whisper local (transcrição)     |
| PostgreSQL       | 16             | Persistência                             |
| Redis            | 8              | Cache distribuído                        |
| RabbitMQ         | 4              | Mensageria orientada a eventos           |
| Ollama           | latest         | LLM local para resumos                   |
| Resilience4j     | 2.4.0          | Circuit Breaker · Retry · Bulkhead       |
| Prometheus       | latest         | Coleta de métricas                       |
| Grafana          | latest         | Dashboards                               |
| Zipkin           | latest         | Distributed tracing                      |
| ArchUnit         | 1.4.2          | Validação automatizada de arquitetura    |
| Testcontainers   | 1.21.0         | Testes de integração                     |
| Docker Compose   | v2+            | Orquestração local                       |

---

## 🏗️ Arquitetura

O projeto segue a **Arquitetura Hexagonal (Ports & Adapters)** organizada em 3 bounded contexts independentes:

```
com.erichiroshi.speechaihexagonal/
├── transcription/     ← Transcrição de áudio (núcleo do negócio)
├── analysis/          ← Resumo via LLM (Ollama)
├── notification/      ← Notificações multicanal (Email/SMS/WhatsApp)
└── shared/            ← Configurações compartilhadas (RabbitMQ)
```

Cada bounded context segue o padrão:

```
{contexto}/
├── domain/            ← Java puro: entidades, eventos, exceções, serviços de domínio
├── application/       ← Casos de uso + portas (in/out)
└── infrastructure/    ← Adapters: HTTP, JPA, Redis, RabbitMQ, LLM
```

### Por que hexagonal?

- **Trocar Speaches por OpenAI** → novo `OpenAiSpeechAdapter implements SpeechToTextPort`. Domínio não muda.
- **Trocar Ollama por GPT-4** → novo adapter implementando `LanguageModelPort`. Use case não muda.
- **Adicionar canal de notificação** → novo adapter implementando `NotificationPort`. Use case não muda.
- **Testar use cases** → Mockito puro, sem Spring, execução < 100ms.

### Fluxo de transcrição

```
POST /api/transcriptions
  → TranscriptionController
  → TranscribeAudioUseCase
      1. Valida arquivo (tamanho ≤ 5MB, Content-Type)
      2. Gera audioHash SHA-256
      3. Redis hit? → retorna + publica evento
      4. PostgreSQL hit? → popula Redis + retorna + publica evento
      5. SpeechToTextPort → SpeachesAdapter/OpenAiSpeechAdapter → Speaches/OpenAI
      6. Persiste PostgreSQL + popula Redis
      7. Publica TranscriptionCompletedEvent → RabbitMQ
  → TranscriptionResponse { id, audioHash, audioTranscription, createdAt }
```

### Fluxo de notificação (eventos)

```
TranscriptionCompletedEvent / SummaryCompletedEvent
  → RabbitMQ (exchange)
  → NotificationEventConsumer
  → SendNotificationUseCase
  → NotificationFactory → adapter correto
  → Email | SMS | WhatsApp
```

---

## ⚙️ Pré-requisitos

- Java 25+
- Docker + Docker Compose v2+
- GPU NVIDIA (opcional — Speaches funciona em CPU, porém mais lento)

---

## 🚀 Quick Start

### Modo desenvolvimento (API local + infra via Docker)

```bash
# 1. Clone o repositório
git clone https://github.com/erichiroshi/speech-ai-hexagonal.git
cd speech-ai-hexagonal

# 2. Suba a infraestrutura
docker compose -f docker-compose.dev.yml up -d

# 3. Baixe o modelo Whisper na primeira execução
uvx speaches-cli model download Systran/faster-whisper-small

# 4. Suba a API (provider padrão: Speaches)
./gradlew bootRun --args='--spring.profiles.active=dev'

# 4.1 Ou com OpenAI Whisper
./gradlew bootRun --args='--spring.profiles.active=dev,openai'
```

### Modo produção (tudo via Docker)

```bash
docker compose up -d
```

---

## 📡 Endpoints

### `POST /api/transcriptions`

Transcreve um arquivo de áudio. Reutiliza transcrição existente via SHA-256.

```bash
curl -X POST http://localhost:8080/api/transcriptions \
  -F 'file=@audio.wav;type=audio/wav' | jq .
```

**Resposta 200:**
```json
{
  "id": "3fa85f64-...",
  "audioHash": "a3f2c1...",
  "audioTranscription": "Olá, este é o texto transcrito do áudio.",
  "createdAt": "2025-01-01T10:00:00"
}
```

**Tipos aceitos:** `audio/wav`, `audio/wave`, `audio/mpeg`, `audio/mp3`, `audio/mp4`, `audio/webm`, `audio/ogg`
**Tamanho máximo:** 5 MB

---

### `POST /api/transcriptions/{audioHash}/analysis`

Gera ou recupera um resumo da transcrição via LLM (Ollama).

```bash
curl -X POST http://localhost:8080/api/transcriptions/{audioHash}/analysis | jq .
```

**Resposta 200:**
```json
{
  "id": "7ab12c3d-...",
  "audioHash": "a3f2c1...",
  "summary": "O áudio aborda os principais pontos sobre...",
  "model": "llama3.2:1b",
  "cached": false,
  "createdAt": "2025-01-01T10:00:05"
}
```

---

## 🔧 Variáveis de ambiente

Copie `.env.example` para `.env` e ajuste conforme necessário.

| Variável                     | Padrão                                   | Descrição                              |
|-----------------------------|------------------------------------------|----------------------------------------|
| `DB_URL`                    | `jdbc:postgresql://postgres:5432/speech_ai` | URL do PostgreSQL                  |
| `DB_USERNAME`               | `speech_user`                            | Usuário do PostgreSQL                  |
| `DB_PASSWORD`               | `speech_pass`                            | Senha do PostgreSQL                    |
| `REDIS_HOST`                | `redis`                                  | Host do Redis                          |
| `REDIS_PORT`                | `6379`                                   | Porta do Redis                         |
| `CACHE_TTL`                 | `24h`                                    | TTL das transcrições no Redis          |
| `RABBITMQ_HOST`             | `rabbitmq`                               | Host do RabbitMQ                       |
| `RABBITMQ_PORT`             | `5672`                                   | Porta AMQP do RabbitMQ                 |
| `RABBITMQ_USER`             | `guest`                                  | Usuário do RabbitMQ                    |
| `RABBITMQ_PASS`             | `guest`                                  | Senha do RabbitMQ                      |
| `SPEACHES_BASE_URL`         | `http://speaches:8000`                   | URL do servidor Speaches               |
| `SPEACHES_MODEL`            | `Systran/faster-whisper-small`           | Modelo Whisper                         |
| `SPEECH_PROVIDER`           | `speaches`                               | Provider de transcrição (`speaches` ou `openai`) |
| `OPENAI_API_KEY`            | —                                        | Chave da API OpenAI (provider openai)  |
| `OPENAI_MODEL`              | `whisper-1`                              | Modelo OpenAI para transcrição         |
| `OLLAMA_BASE_URL`           | `http://ollama:11434`                    | URL do servidor Ollama                 |
| `OLLAMA_MODEL`              | `llama3.2:1b`                           | Modelo Ollama para resumo              |
| `SUMMARY_TTL`               | `72h`                                    | TTL dos resumos no Redis               |
| `ZIPKIN_ENDPOINT`           | `http://zipkin:9411/api/v2/spans`        | Endpoint do Zipkin                     |
| `SMTP_HOST`                 | `smtp.gmail.com`                         | Host SMTP para e-mail                  |
| `SMTP_PORT`                 | `587`                                    | Porta SMTP                             |
| `SMTP_USER`                 | —                                        | Usuário SMTP                           |
| `SMTP_PASS`                 | —                                        | Senha SMTP                             |
| `NOTIFICATION_EMAIL_ENABLED`| `false`                                  | Habilita canal de e-mail               |
| `NOTIFICATION_SMS_ENABLED`  | `false`                                  | Habilita canal de SMS                  |
| `NOTIFICATION_WA_ENABLED`   | `false`                                  | Habilita canal de WhatsApp             |
| `NOTIFICATION_EMAIL`        | —                                        | Destinatário das notificações por e-mail |
| `NOTIFICATION_SMS_NUMBER`   | —                                        | Destinatário das notificações por SMS  |
| `NOTIFICATION_WHATSAPP_NUMBER` | —                                     | Destinatário das notificações por WhatsApp |
| `SONAR_TOKEN`               | —                                        | Token SonarCloud                       |

---

## 📊 Observabilidade

| Ferramenta       | URL local                              | Descrição              |
|-----------------|----------------------------------------|------------------------|
| Prometheus       | `http://localhost:9090`               | Coleta de métricas     |
| Grafana          | `http://localhost:3000`               | Dashboards             |
| Zipkin           | `http://localhost:9411`               | Distributed tracing    |
| RabbitMQ Console | `http://localhost:15672`              | Gestão de filas        |
| Actuator Health  | `http://localhost:8080/actuator/health` | Health check         |
| Actuator Prometheus | `http://localhost:8080/actuator/prometheus` | Métricas raw    |

### Métricas customizadas

| Métrica                         | Tipo              | Descrição                              |
|--------------------------------|-------------------|----------------------------------------|
| `transcription.requests.total` | Counter (status)  | Total de requisições por status        |
| `transcription.cache.hits.total` | Counter (layer) | Cache hits por camada (redis/db)       |
| `transcription.speaches.duration` | Timer (p50/p95/p99) | Duração das chamadas ao Speaches  |
| `transcription.file.size.bytes` | DistributionSummary | Tamanho dos arquivos enviados      |

### Tracing

Todos os traces são coletados (`sampling.probability=1.0`) e enviados ao Zipkin. O `traceId` e `spanId` aparecem automaticamente nos logs JSON (MDC).

---

## 🧪 Testes

```bash
# Rodar todos os testes
./gradlew test

# Relatório de cobertura
./gradlew test jacocoTestReport
# Acessar: build/reports/jacoco/test/html/index.html
```

### Estratégia de testes

| Classe | Tipo | Ferramentas |
|--------|------|-------------|
| `TranscribeAudioUseCaseTest` | Unitário | JUnit 5 + Mockito |
| `SummarizeTranscriptionUseCaseTest` | Unitário | JUnit 5 + Mockito |
| `SendNotificationUseCaseTest` | Unitário | JUnit 5 + Mockito |
| `TranscriptionControllerTest` | Slice | `@WebMvcTest` + `@MockitoBean` |
| `AnalysisControllerTest` | Slice | `@WebMvcTest` + `@MockitoBean` |
| `SpeachesAdapterTest` | Unitário + HTTP mock | MockWebServer |
| `SpeachesAdapterResilienceIT` | Integração | WireMock + CircuitBreakerRegistry |
| `RedisCacheAdapterIT` | Integração | Testcontainers Redis |
| `PostgresRepositoryAdapterTest` | Integração | Testcontainers PostgreSQL |
| `TranscriptionEventPublisherIT` | Integração | Testcontainers RabbitMQ |
| `HexagonalArchitectureTest` | Arquitetural | ArchUnit |

---

## 📁 Estrutura do projeto

```
speech-ai-hexagonal/
├── src/main/java/.../
│   ├── transcription/                    ← Bounded context: transcrição
│   │   ├── domain/
│   │   │   ├── model/Transcription.java
│   │   │   ├── event/TranscriptionCompletedEvent.java
│   │   │   ├── service/AudioHashService.java
│   │   │   └── exception/
│   │   ├── application/
│   │   │   ├── port/in/TranscribeAudioPort.java
│   │   │   ├── port/out/               ← SpeechToTextPort, CachePort, RepositoryPort, MetricsPort, EventPublisherPort
│   │   │   └── TranscribeAudioUseCase.java
│   │   └── infrastructure/
│   │       ├── http/                   ← Controller, Response, GlobalExceptionHandler, MdcLoggingFilter
│   │       ├── persistence/            ← PostgresRepositoryAdapter, TranscriptionEntity
│   │       ├── cache/                  ← RedisCacheAdapter, InMemoryCacheAdapter
│   │       ├── speechtotext/           ← SpeachesAdapter, OpenAiSpeechAdapter
│   │       ├── metrics/                ← TranscriptionMetricsAdapter
│   │       └── messaging/              ← RabbitMqTranscriptionEventPublisher, TranscriptionAuditConsumer
│   ├── analysis/                         ← Bounded context: resumo via LLM
│   │   ├── domain/
│   │   │   ├── model/Summary.java
│   │   │   ├── event/SummaryCompletedEvent.java
│   │   │   └── exception/
│   │   ├── application/
│   │   │   ├── port/in/SummarizeTranscriptionPort.java
│   │   │   ├── port/out/               ← LanguageModelPort, SummaryStorePort, TranscriptionTextPort, SummaryEventPublisherPort
│   │   │   └── SummarizeTranscriptionUseCase.java
│   │   └── infrastructure/
│   │       ├── http/                   ← AnalysisController, AnalysisResponse
│   │       ├── llm/ollama/             ← OllamaLanguageModelAdapter
│   │       ├── cache/redis/            ← RedisStoreAdapter, TranscriptionTextAdapter
│   │       └── messaging/              ← RabbitMqSummaryEventPublisher, SummaryAuditConsumer
│   ├── notification/                     ← Bounded context: notificações
│   │   ├── domain/
│   │   │   ├── model/                  ← Notification, NotificationChannel, NotificationType, NotificationId
│   │   │   └── exception/
│   │   ├── application/
│   │   │   ├── port/in/SendNotificationPort.java
│   │   │   ├── port/out/NotificationPort.java
│   │   │   ├── SendNotificationUseCase.java
│   │   │   └── NotificationFactory.java
│   │   └── infrastructure/
│   │       ├── channel/                ← EmailNotificationAdapter, SmsNotificationAdapter, WhatsAppNotificationAdapter, NoOpNotificationAdapter
│   │       └── messaging/              ← NotificationEventConsumer, NotificationRabbitMqConfig
│   └── shared/
│       └── rabbitmq/RabbitMqConfig.java ← RabbitTemplate + serialização JSON
├── src/test/
│   └── architecture/HexagonalArchitectureTest.java
├── observability/                        ← Prometheus + Grafana dashboards
├── docs/                                 ← GitHub Pages
├── docker-compose.yml
├── docker-compose.dev.yml
└── Dockerfile
```

---

## ⚠️ Troubleshooting

**Speaches demorando para responder:**
O modelo `faster-whisper-small` precisa ser carregado na primeira requisição. Aguarde ~30s após o container subir.

**`SpeechToTextException: Speaches retornou resposta vazia`:**
O Speaches pode retornar texto vazio para áudios com silêncio ou ruído alto. Verifique o arquivo de áudio.

**GPU não detectada pelo Docker:**
Certifique-se de ter o NVIDIA Container Toolkit instalado. Para rodar em CPU, remova o bloco `deploy.resources` do `docker-compose.yml`.

**CircuitBreaker OPEN (503):**
O Speaches falhou mais de 50% das chamadas em uma janela de 10. Aguarde 30s para transição automática para HALF_OPEN.

**RabbitMQ connection refused:**
Aguarde o healthcheck do container (`rabbitmq-diagnostics ping`). Pode levar até 30s na primeira inicialização.

---

## Autor

**Eric Hiroshi** — Backend Engineer

[![GitHub](https://img.shields.io/badge/GitHub-erichiroshi-181717?logo=github)](https://github.com/erichiroshi)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-eric--hiroshi-0077B5?logo=linkedin)](https://linkedin.com/in/eric-hiroshi)
