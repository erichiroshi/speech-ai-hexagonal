<p align="center">
  <img width="25%" src="./images/logo eric hiroshi.png" alt="Eric Hiroshi Logo">
</p>

<h1 align="center">🎙️ Speech Ai Hexagonal</h1>

> API REST de transcrição de áudio construída com **arquitetura hexagonal** (Ports & Adapters), Java 25 e Spring Boot 4.


<p align="center">
  <img src="https://img.shields.io/badge/Java-25-red?style=flat-square&logo=openjdk" alt="Java 25">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 4.0.6">
  <img src="https://img.shields.io/badge/Speaches-Whisper-4A90D9?style=flat-square" alt="Speaches Whisper">
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white" alt="Docker Compose">
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="MIT License">
</p>

---

## 🗺️ Roadmap

| Fase | Descrição | Status |
|------|-----------|--------|
| **1** | Base hexagonal — transcrição local (Speaches/Whisper) · RestClient · Lombok · MapStruct | ✅ `v1.0.0` |
| **2** | Cache Redis com SHA-256 — RedisConfig · RedisCacheAdapter · Testcontainers | 🔜 `v2.x` |
| **3** | Observabilidade — Prometheus · Grafana · Zipkin/OTel · Logs JSON + MDC | 🔜 `v3.x` |
| **4** | Resiliência — Circuit Breaker · Retry · Bulkhead (Resilience4j) | 🔜 `v4.x` |
| **5** | Spring AI + OpenAI Whisper — segunda porta de saída (cloud) | 🔜 `v5.x` |
| **6** | Spring AI + Ollama — resumo por LLM local ≤1B parâmetros | 🔜 `v6.x` |
| **7** | RabbitMQ — TranscriptionCompletedEvent · DLQ · Consumer de auditoria | 🔜 `v7.x` |
| **8** | CI/CD — GitHub Actions · SonarCloud · Codecov · Docker Hub · Multi-arch | 🔜 `v8.x` |

---

## 🌐 Documentação

> GitHub Pages — em construção a partir da Fase 3.

---

## 🛠️ Stack

| Tecnologia | Versão | Papel |
|-----------|--------|-------|
| Java | 25 | Linguagem |
| Spring Boot | 4.0.6 | Framework web + DI |
| Speaches | latest-cuda | Servidor Whisper (transcrição local) |
| Whisper | faster-whisper-small | Modelo de transcrição |
| Docker Compose | v2+ | Orquestração local |

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
```

### Modo produção (tudo via Docker)

```bash
docker compose up -d
```

---

## 📄 Swagger UI
> Ainda não implementado.

Disponível em: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

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

## 📊 Observabilidade

> Disponível a partir da Fase 3.

| Ferramenta | URL local | Descrição |
|-----------|-----------|-----------|
| Prometheus | `http://localhost:9090` | Métricas |
| Grafana | `http://localhost:3000` | Dashboards |
| Zipkin | `http://localhost:9411` | Tracing |
| Actuator Health | `http://localhost:8080/actuator/health` | Health check |

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

**Eric Hiroshi** — Backend Engineer

[![GitHub](https://img.shields.io/badge/GitHub-erichiroshi-181717?logo=github)](https://github.com/erichiroshi)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-eric--hiroshi-0077B5?logo=linkedin)](https://linkedin.com/in/eric-hiroshi)
