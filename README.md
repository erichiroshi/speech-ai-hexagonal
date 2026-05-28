<p align="center">
  <img width="25%" src="./images/logo eric hiroshi.png" alt="Eric Hiroshi Logo">
</p>

<h1 align="center">🎙️ Speech Ai Hexagonal</h1>

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
  <img src="https://img.shields.io/badge/Resilience4j-informational?style=flat-square" alt="Resilience4j">
  <img src="https://img.shields.io/badge/OpenAPI-3.1-6BA539?style=flat-square&logo=openapiinitiative&logoColor=white" alt="OpenAPI 3.1">  
  <img src="https://img.shields.io/badge/Jacoco-70%25-brightgreen?style=flat-square" alt="Jacoco">
  <img src="https://img.shields.io/badge/SonarCloud-passing-4E9BCD?style=flat-square&logo=sonarcloud&logoColor=white" alt="SonarCloud">
  <img src="https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?style=flat-square&logo=githubactions&logoColor=white" alt="GitHub Actions">
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="MIT License">
</p>

- [🗺️ Roadmap](#️-roadmap)
- [🌐 Documentação](#-documentação)
- [🛠️ Stack](#️-stack)
- [🏗️ Arquitetura](#️-arquitetura)
  - [Por que hexagonal?](#por-que-hexagonal)
- [⚙️ Pré-requisitos](#️-pré-requisitos)
- [🚀 Quick Start](#-quick-start)
  - [Modo desenvolvimento (API local + Speaches via Docker)](#modo-desenvolvimento-api-local--speaches-via-docker)
  - [Modo produção (tudo via Docker)](#modo-produção-tudo-via-docker)
- [SonarQube local](#sonarqube-local)
  - [Subir infraestrutura](#subir-infraestrutura)
  - [Acessar dashboard](#acessar-dashboard)
- [📄 Swagger UI](#-swagger-ui)
- [Bounded Context de Análise](#bounded-context-de-análise)
  - [Recursos adicionados](#recursos-adicionados)
- [Endpoint de análise](#endpoint-de-análise)
  - [Response](#response)
- [Estratégia de cache](#estratégia-de-cache)
  - [Fluxo](#fluxo)
- [📡 Endpoint de transcrição](#-endpoint-de-transcrição)
  - [`POST /api/transcriptions`](#post-apitranscriptions)
  - [`POST /api/transcriptions/{hashTranscription}/analysis`](#post-apitranscriptionshashtranscriptionanalysis)
- [Multi-provider Speech-to-Text](#multi-provider-speech-to-text)
  - [Providers disponíveis](#providers-disponíveis)
- [OpenAI Whisper](#openai-whisper)
  - [Seleção dinâmica](#seleção-dinâmica)
- [Ativação do profile OpenAI](#ativação-do-profile-openai)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Persistência](#persistência)
  - [Tecnologias](#tecnologias)
  - [Audio Hash](#audio-hash)
  - [Objetivos](#objetivos)
  - [Exemplo](#exemplo)
- [📊 Observabilidade](#-observabilidade)
  - [Componentes](#componentes)
  - [Métricas](#métricas)
  - [Tracing](#tracing)
  - [Logs estruturados](#logs-estruturados)
- [Governança Arquitetural](#governança-arquitetural)
  - [Validações adicionadas](#validações-adicionadas)
  - [Exemplo](#exemplo-1)
  - [Objetivos](#objetivos-1)
- [🧪 Testando a API](#-testando-a-api)
  - [Estratégia de testes](#estratégia-de-testes)
- [🔧 Variáveis de ambiente](#-variáveis-de-ambiente)
- [🔀 Fluxo real de execução](#-fluxo-real-de-execução)
- [📁 Estrutura do projeto](#-estrutura-do-projeto)
- [⚠️ Troubleshooting](#️-troubleshooting)
- [Autor](#autor)

---

## 🗺️ Roadmap

| Fase   | Descrição | Status     |
|--------|-----------|------------|
| **1**  | Base hexagonal — transcrição local (Speaches/Whisper) · RestClient · Lombok · MapStruct | ✅ `v1.7.0` |
| **2**  | Qualidade de código com SonarQube e JaCoCo | ✅ `v2.5.0` |
| **3**  | Setup PostgreSQL + JPA + Flyway | ✅ `v3.7.0` |
| **4**  | Cache Redis com SHA-256 — RedisConfig · RedisCacheAdapter · Testcontainers | ✅ `v4.3.0` |
| **5**  | Resiliência — Circuit Breaker · Retry · Bulkhead (Resilience4j) | ✅ `v5.3.0` |
| **6**  | Observabilidade — Prometheus · Grafana · Zipkin/OTel · Logs JSON + MDC | ✅ `v6.5.0` |
| **7**  | Validação automatizada da arquitetura hexagonal| ✅ `v7.1`   |
| **8**  | Spring AI + OpenAI Whisper — segunda porta de saída (cloud) | ✅ `v8.2`   |
| **9**  | Spring AI + Ollama — resumo por LLM local ≤1B parâmetros | ✅ `v9.3`   |
| **10** | RabbitMQ — TranscriptionCompletedEvent · DLQ · Consumer de auditoria | 🔜 `v10.x` |
| **11** | CI/CD — GitHub Actions · SonarCloud · Codecov · Docker Hub · Multi-arch | 🔜 `v11.x` |

---

## 🌐 Documentação


👉 https://github.com/erichiroshi/speech-ai-hexagonal
 
| Página                                                                             | Descrição                                      |
|------------------------------------------------------------------------------------|------------------------------------------------|
| [Home](https://erichiroshi.github.io/speech-ai-hexagonal/)                         | Visão geral e quick start                      |
| [Arquitetura](https://erichiroshi.github.io/speech-ai-hexagonal/architecture.html) | C4 Model — Contexto, Containers, Componentes   |
| [Qualidade](https://erichiroshi.github.io/speech-ai-hexagonal/quality.html)        | Jacoco + SonarCloud + Codecov                  |
| [Persistência](https://erichiroshi.github.io/speech-ai-hexagonal/persistence.html) | Postgres + Flyway                              |
| [Cache](https://erichiroshi.github.io/speech-ai-hexagonal/cache.html)            | Cache Redis Distribuído                        |
| [Observabilidade](https://erichiroshi.github.io/speech-ai-hexagonal/observability.html) | Promethes + Grafana + Zipkin + Logs JSON (MDC) |
| [Roadmap](https://erichiroshi.github.io/speech-ai-hexagonal/roadmap.html)          | Roadmap do projeto                             |
| [Resiliência]() | -                                              |
| [API Reference]() | -                                              |

---

## 🛠️ Stack

| Tecnologia | Versão | Papel |
|-----------|--------|-------|
| Java | 25 | Linguagem |
| Spring Boot | 4.0.6 | Framework web + DI |
| Speaches | latest-cuda | Servidor Whisper (transcrição local) |
| Whisper | faster-whisper-small | Modelo de transcrição |
| Docker Compose | v2+ | Orquestração local |
| Build Tool | Gradle |
| Arquitetura | Hexagonal |
| IA local | Whisper |
| Documentação API | Swagger/OpenAPI |
| Qualidade | JaCoCo + SonarQube |
| Observabilidade | Prometheus + Grafana |
| Mensageria | RabbitMQ |
| Cache | Redis |

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                 BOUNDED CONTEXT: transcription/                 │
├──────────────────┬────────────────────────┬─────────────────────┤
│   ADAPTER IN     │     APPLICATION        │    ADAPTER OUT      │
│                  │                        │                     │
│  Transcription   │  TranscribeAudioPort   │  SpeachesAdapter    │
│  Controller      │  (interface)           │  implements         │
│   │ injeta       │    ↑ implements        │  SpeechToTextPort   │
│   ▼              │  TranscribeAudioUseCase│                     │
│  TranscribeAudio │  + validação           │  → Fase 5:          │
│  Port (interface)│  + mapeamento          │  OpenAiSpeechAdapter│
│                  │                        │                     │
│            domain/model/Transcription     │                     │
│            (zero dependência de Spring)   │                     │
└──────────────────┴────────────────────────┴─────────────────────┘
```

### Por que hexagonal?

- **Trocar Speaches por OpenAI Whisper** → novo `OpenAiSpeechAdapter` implements `SpeechToTextPort`. Domínio não muda.
- **Adicionar cache Redis** → novo `RedisCacheAdapter` implements `TranscriptionCachePort`. Use case não muda.
- **Testar o Controller** → `@WebMvcTest` + `@MockitoBean TranscribeAudioPort`. Sem contexto completo.
- **Testar o use case** → Mockito puro. Execução < 100ms. Sem Spring.

---

## ⚙️ Pré-requisitos

- Java 25+
- Docker + Docker Compose v2+
- GPU NVIDIA (opcional — Speaches funciona em CPU, mas mais lento)

---

## 🚀 Quick Start

### Modo desenvolvimento (API local + Speaches via Docker)

```bash
# 1. Clone o repositório
git clone https://github.com/erichiroshi/speech-ai-hexagonal.git
cd speech-ai-hexagonal

# 2. Suba o Speaches
docker compose -f docker-compose.dev.yml up -d

# 3. Baixe o modelo na primeira execução
uvx speaches-cli model download Systran/faster-whisper-small

# 4. Suba a API
./gradlew bootRun --args='--spring.profiles.active=dev'

# 4.1 Suba a API usando OPENAI
./gradlew bootRun --args='--spring.profiles.active=dev,openai'
```

### Modo produção (tudo via Docker)

```bash
docker compose up -d
```

---

## SonarQube local

O ambiente local executa SonarQube via Docker Compose.

### Subir infraestrutura

```bash
docker compose -f docker-compose.dev.yml up -d
```

### Acessar dashboard

```text
http://localhost:9000
```

---

## 📄 Swagger UI
> Ainda não implementado.

Disponível em: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Bounded Context de Análise

A aplicação agora possui o contexto `analysis/`
dedicado à geração de resumos utilizando modelos LLM.

### Recursos adicionados

- LanguageModelPort
- SummarizeTranscriptionUseCase
- OllamaLanguageModelAdapter
- Cache Redis para resumos

---

## Endpoint de análise

```http
POST /api/transcriptions/{audioHash}/analysis
```

### Response

```json
{
  "summary": "...",
  "audioHash": "...",
  "model": "qwen2.5:0.5b",
  "cached": false
}
```

---

## Estratégia de cache

```text
summary:{hash}
```

### Fluxo

```text
1ª chamada → LLM → Redis
2ª chamada → Redis hit
```

---

## 📡 Endpoint de transcrição

### `POST /api/transcriptions`

```bash
curl -X POST http://localhost:8080/api/transcriptions \
  -F 'file=@audio.wav;type=audio/wav' | jq .
```

**Resposta 200:**

```json
{
  "audioTranscription": "Olá, este é o texto transcrito do áudio."
}
```

**Tipos aceitos:** `audio/wav`, `audio/wave`, `audio/mpeg`, `audio/mp3`, `audio/mp4`, `audio/webm`, `audio/ogg`  
**Tamanho máximo:** 5 MB

---

### `POST /api/transcriptions/{hashTranscription}/analysis`

```bash
curl -X POST http://localhost:8080/api/transcriptions/{hashTranscription}/analysis \
```

**Resposta 200:**

```json
{
  "summary": "Este é o resumo do áudio transcrito."
}
```

---

## Multi-provider Speech-to-Text

A aplicação agora suporta múltiplos providers de transcrição utilizando arquitetura hexagonal.

### Providers disponíveis

| Provider | Estratégia |
|---|---|
| speaches | Local/GPU |
| openai | Cloud/OpenAI Whisper |

---

## OpenAI Whisper

Implementação baseada em:

- Spring AI
- AudioTranscriptionModel
- whisper-1

### Seleção dinâmica

```yaml
# local
speech.provider=speaches

# cloud
speech.provider=openai
```

---

## Ativação do profile OpenAI

```bash
./gradlew bootRun --args='--spring.profiles.active=dev,openai'
```

---

## Variáveis de ambiente

```env
OPENAI_API_KEY=sk-...
```

---

## Persistência

A aplicação agora possui infraestrutura PostgreSQL integrada com Spring Data JPA e Flyway.  
[Documentação](https://erichiroshi.github.io/speech-ai-hexagonal/persistence.html)

### Tecnologias

- PostgreSQL 16
- Spring Data JPA
- Hibernate
- Flyway
- HikariCP

---

### Audio Hash

A aplicação agora possui um serviço dedicado para geração determinística do `audioHash` utilizando SHA-256 hexadecimal.

### Objetivos

- Identificação única do conteúdo binário
- Preparação para deduplicação futura
- Reutilização entre use cases
- Encapsulamento da lógica criptográfica

### Exemplo

```java
MessageDigest md = MessageDigest.getInstance("SHA-256");

byte[] hash = md.digest(audioBytes);

return HexFormat.of().formatHex(hash);
```

---

## 📊 Observabilidade

| Ferramenta | URL local | Descrição |
|-----------|-----------|-----------|
| Prometheus | `http://localhost:9090` | Métricas |
| Grafana | `http://localhost:3000` | Dashboards |
| Zipkin | `http://localhost:9411` | Tracing |
| Actuator Health | `http://localhost:8080/actuator/health` | Health check |

A aplicação agora possui stack completa de observabilidade.

### Componentes

- Micrometer
- Prometheus
- Grafana
- OpenTelemetry
- Zipkin
- Logs JSON estruturados

---

### Métricas

```java
Counter.builder("transcription.requests.total")
  .tag("status","success")
  .register(registry);
```

---

### Tracing

```yaml
management.tracing.sampling.probability=1.0
management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans
```

---

### Logs estruturados

Campos MDC:

```text
requestId
traceId
spanId
httpMethod
uri
fileName
```

---

## Governança Arquitetural

A aplicação agora possui validação automatizada da arquitetura hexagonal utilizando ArchUnit.

### Validações adicionadas

- Onion Architecture
- Convenções de portas
- Separação domínio/aplicação/infraestrutura
- Restrições de dependência

### Exemplo

```java
@ArchTest
public static final ArchRule arquitetura_hexagonal_completa =
    onionArchitecture()
        .domainModels("..transcription.domain.model..")
        .applicationServices("..transcription.application..")
        .adapter("http", "..transcription.infrastructure.http..");
```

### Objetivos

- Evitar erosão arquitetural
- Reduzir acoplamento indevido
- Garantir governança contínua
- Executar validações junto dos testes

---

## 🧪 Testando a API

```bash
# Verificar health
curl http://localhost:8080/actuator/health | jq .

# Transcrever um arquivo WAV
curl -X POST http://localhost:8080/api/transcriptions \
  -F 'file=@meu-audio.wav;type=audio/wav' | jq .

# Segunda chamada — deve retornar cached: true
curl -X POST http://localhost:8080/api/transcriptions \
  -F 'file=@meu-audio.wav;type=audio/wav' | jq .cached
```

```bash
# Rodar todos os testes
./gradlew test

# Relatório de cobertura (build/reports/jacoco/test/html/index.html)
./gradlew test jacocoTestReport
```

### Estratégia de testes

| Classe | Tipo | Ferramentas |
|--------|------|-------------|
| `TranscribeAudioUseCaseTest` | Unitário | JUnit 5 + Mockito |
| `TranscriptionControllerTest` | Slice test | `@WebMvcTest` + `@MockitoBean` |
| `SpeachesAdapterTest` | Unitário + HTTP mock | JUnit 5 + MockWebServer |
| `SpeechAiHexagonalApplicationTests` | Smoke test | `@SpringBootTest` |

---

## 🔧 Variáveis de ambiente

| Variável | Padrão | Descrição |
|---------|--------|-----------|
| `SPEACHES_BASE_URL` | `http://speaches:8000` | URL do servidor Speaches |
| `SPEACHES_MODEL` | `Systran/faster-whisper-small` | Modelo Whisper |
| SONAR_HOST_URL | `http://localhost:9000` | URL do servidor SonarQube |
| SONAR_TOKEN | `local` | Token de autenticação |
| SONAR_PROJECT_KEY | `speech-ai-hexagonal` | Chave do projeto |
| SPRING_DATASOURCE_URL | `jdbc:postgresql://localhost:5432/speech_ai` | RUL do banco de dados
| SPRING_DATASOURCE_USERNAME | `postgres` | Username do postgres
| SPRING_DATASOURCE_PASSWORD | `postgres` | Password do postgres

Copie `.env.example` para `.env` e ajuste conforme necessário.

---

## 🔀 Fluxo real de execução

```
POST /api/transcriptions
        │
        ▼
TranscriptionController
  valida arquivo não-nulo
        │
        ▼
TranscribeAudioUseCase
  1. valida: tamanho ≤5MB, Content-Type permitido
  2. speechToText.transcribe(bytes, filename, contentType)
        │
        ▼
  SpeachesAdapter
    RestClient → POST http://speaches:8000/v1/audio/transcriptions
    (multipart: file + model + response_format=json)
        │
        ▼
  SpeachesResponse → Transcription
        │
        ▼
  3. retorna Transcription
        │
        ▼
TranscriptionResponse { audioTranscription }
```

---

## 📁 Estrutura do projeto

```
speech-ai-hexagonal/
├── src/main/java/.../transcription
│   ├── domain/
│   │   ├── model/Transcription.java
│   │   ├── port/out/SpeechToTextPort.java
│   │   └── exception/AudioValidationException.java
│   │                 SpeechToTextException.java
│   ├── application/
│   │   ├── port/in/TranscribeAudioPort.java
│   │   ├── TranscribeAudioUseCase.java
│   │   └── input|output|mapper/
│   └── infrastructure/
│       ├── http/TranscriptionController.java
│       │    ├── response/TranscriptionResponse.java
│       │    └── handler/GlobalExceptionHandler.java
│       └── speechtotext/speaches/SpeachesAdapter.java
│                            ├──  SpeachesProperties.java
│                            ├──  config/RestClientConfig.java
│                            └──  response/SpeachesResponse.java
├── frontend/         ← UI dark theme (HTML/CSS/JS)
├── about/            ← documentação por fase
├── docs/             ← GitHub Pages
├── docker-compose.yml
├── docker-compose.dev.yml
└── Dockerfile
```

---

## ⚠️ Troubleshooting

**Speaches demorando para responder:**
O modelo `faster-whisper-small` precisa ser carregado na primeira requisição. Aguarde ~30s após o container subir.

**`SpeechToTextException: Speaches retornou resposta vazia`:**
O Speaches pode retornar texto vazio para áudios com silêncio ou ruído muito alto. Verifique o áudio.

**GPU não detectada pelo Docker:**
Certifique-se de ter o NVIDIA Container Toolkit instalado. Em CPU, remova o bloco `deploy.resources` do `docker-compose.yml`.

---

## Autor
S
**Eric Hiroshi** — Backend Engineer

[![GitHub](https://img.shields.io/badge/GitHub-erichiroshi-181717?logo=github)](https://github.com/erichiroshi)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-eric--hiroshi-0077B5?logo=linkedin)](https://linkedin.com/in/eric-hiroshi)
