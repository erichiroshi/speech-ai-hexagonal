# Fase 1 — Base Hexagonal + Transcrição Local

**Versão:** `v1.0.0`  
**Status:** ✅ Concluída

---

## O que foi implementado

### Estrutura hexagonal

Criação do projeto com separação estrita entre as três camadas do hexágono:

```
br.com.erichiroshi.speechai/
├── domain/
│   ├── model/Transcription.java           ← agregado central
│   └── exception/
│       ├── AudioValidationException.java
│       └── SpeechToTextException.java
├── application/
│   ├── port/
│   │   ├── in/TranscribeAudioUseCase.java  ← porta de entrada
│   │   └── out/
│   │       ├── SpeechToTextPort.java       ← porta de saída: motor de transcrição
│   │       └── TranscriptionCachePort.java ← porta de saída: cache
│   └── service/TranscriptionService.java  ← use case
└── infrastructure/
    ├── adapter/
    │   ├── in/web/
    │   │   ├── TranscriptionController.java
    │   │   ├── dto/TranscriptionResponse.java
    │   │   └── handler/GlobalExceptionHandler.java
    │   └── out/
    │       ├── speaches/SpeachesAdapter.java
    │       └── cache/InMemoryCacheAdapter.java
    └── config/
        ├── AppProperties.java
        └── WebClientConfig.java
```

### Domínio (`domain/`)

- **`Transcription`**: modelo simples com `text` — sem anotações Spring
- **`AudioValidationException`**: campo `field` + mensagem → HTTP 400
- **`SpeechToTextException`**: falha no motor → HTTP 502
- **`SpeechToTextPort`**: `transcribe(byte[], String filename, String contentType)` — preparado para Fase 5

### Portas (`application/port/`)

- **`TranscribeAudioUseCase`** (entrada): contrato que o controller usa. Implementado por `TranscriptionService`.
- **`SpeechToTextPort`** (saída): abstrai o motor de transcrição. Implementado por `SpeachesAdapter` (Fase 1) e, futuramente, `OpenAiSpeechAdapter` (Fase 5).

### Use Case (`TranscribeAudioUseCase`)

Implementa `TranscribeAudioPort`. Valida:
- `audioBytes` não nulo e não vazio → `AudioValidationException`
- tamanho ≤ 5 MB → `AudioValidationException`
- `contentType` ∈ `{audio/wav, audio/wave, audio/mpeg, audio/mp3, audio/mp4, audio/webm, audio/ogg}` → `AudioValidationException`

Delega para `SpeechToTextPort`. Mapeia `Transcription → TranscriptionOutput` via `TranscriptionMapper`.

### Adapter de saída (`SpeachesAdapter`)

Implementa `SpeechToTextPort` via `RestClient`:
- `POST /v1/audio/transcriptions` (API compatível OpenAI)
- `ByteArrayResource` com `filename` preservado do arquivo original
- `onStatus(isError)` → `SpeechToTextException` com status + body
- Resposta vazia → `SpeechToTextException`
- `SpeachesMapper` (MapStruct) mapeia `SpeachesResponse → Transcription`

### Adapter de entrada

**`TranscriptionController`**:
- `POST /api/transcriptions` — `multipart/form-data`
- Delega bytes + filename + contentType para o use case
- Retorna `TranscriptionResponse` com text, audioHash, cached, createdAt
- Anotações Swagger/OpenAPI completas

**`GlobalExceptionHandler`**:
- `AudioValidationException` → `400 Bad Request` + ProblemDetail RFC 9457
- `SpeechToTextException` → `502 Bad Gateway` + ProblemDetail RFC 9457
- `MaxUploadSizeExceededException` → `400 Bad Request`
- `Exception` → `500 Internal Server Error`

---

## Arquivos alterados/criados

```
build.gradle
settings.gradle
Dockerfile
docker-compose.yml
docker-compose.dev.yml
.env / .env.dev / .env.example / .gitignore
CLAUDE.md / VERSIONING.md / release.sh
frontend/index.html / styles.css / script.js
src/main/java/**  (12 arquivos)
src/main/resources/application.yml / application-dev.yml
src/test/java/**  (4 arquivos de teste)
src/test/resources/application-test.yml
about/projeto.md / about/fase-1-base-hexagonal.md
```

---

## Decisões de arquitetura e trade-offs

### Por que `TranscribeAudioPort` como interface?

`TranscribeAudioUseCase` é um `@Service` Spring — um detalhe de infraestrutura.  
O `TranscriptionController` não deve depender de detalhes de infraestrutura.  
A porta de entrada (`TranscribeAudioPort`) é a abstração que isola o Controller do use case concreto, permitindo `@WebMvcTest` com simples `@MockitoBean` sem subir contexto completo.

### Por que `SpeechToTextPort` recebe `filename` e `contentType`?

Decisão prospectiva para a Fase 5: `OpenAiSpeechAdapter` precisa do `filename` para o multipart e do `contentType` para validação. Projetar a porta sem esses parâmetros forçaria uma quebra de contrato na Fase 5 — algo que a arquitetura hexagonal deve evitar.

### Por que `RestClient` e não `WebClient`?

Transcrição de áudio é uma operação inerentemente síncrona e bloqueante (aguarda a GPU processar o áudio). Não há ganho real em usar programação reativa aqui. `RestClient` (Spring 6.1+) é mais simples, não requer a dependência do WebFlux e produz código mais legível.

### Por que MapStruct?

MapStruct gera código em tempo de compilação — zero reflexão em runtime. Cada camada tem seu mapper dedicado: `SpeachesMapper` (infra→domain), `TranscriptionMapper` (domain→output), `TranscriptionHttpMapper` (output→HTTP response). Erros de mapeamento são detectados na compilação, não em runtime.

### Por que validação no use case?

Validação de tamanho e Content-Type é regra de negócio de transcrição, não responsabilidade HTTP. Se existir futuramente um adapter de entrada via fila (RabbitMQ), a mesma validação se aplica — e ela estará no lugar certo.

---

## Troubleshooting

### Speaches não responde ao healthcheck

```bash
# Verificar se o container subiu
docker compose -f docker-compose.dev.yml ps

# Logs do Speaches
docker compose -f docker-compose.dev.yml logs speaches

# O modelo precisa ser baixado na primeira execução (pode demorar)
uvx speaches-cli model download Systran/faster-whisper-small
```

### Erro 400 com Content-Type inesperado

O browser às vezes envia `audio/x-wav` em vez de `audio/wav`. Adicionar ao `ALLOWED_CONTENT_TYPES` no `TranscriptionService` se necessário.

### `SpeechToTextException` em chamadas ao MockWebServer nos testes

Verificar se o `MockWebServer` foi inicializado antes do `WebClient` (o `baseUrl` precisa apontar para a porta do mock).

### `TranscriptionHttpMapper` não encontrado no contexto

Verificar que `componentModel = "spring"` está em **minúsculo**. `"Spring"` (maiúsculo) não é reconhecido pelo MapStruct e o bean não é gerado.

### Modelo não encontrado no Speaches

O modelo precisa ser baixado antes da primeira transcrição:
```bash
uvx speaches-cli model download Systran/faster-whisper-small
```

### `SpeachesAdapter` retorna `SpeechToTextException` com status 404

O endpoint correto é `/v1/audio/transcriptions`. Verificar se o Speaches subiu corretamente:
```bash
docker compose -f docker-compose.dev.yml logs speaches
curl http://localhost:8000/health
```
