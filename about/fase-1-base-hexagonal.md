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

- **`Transcription`**: agregado central com builder imutável. Campos: `audioHash`, `text`, `filename`, `createdAt`, `cached`. Método `asCached()` retorna nova instância marcada como hit de cache.
- **`AudioValidationException`**: lançada para arquivo vazio, tipo inválido ou tamanho excedido → HTTP 400.
- **`SpeechToTextException`**: lançada quando o motor de transcrição falha → HTTP 502.

### Portas (`application/port/`)

- **`TranscribeAudioUseCase`** (entrada): contrato que o controller usa. Implementado por `TranscriptionService`.
- **`SpeechToTextPort`** (saída): abstrai o motor de transcrição. Implementado por `SpeachesAdapter` (Fase 1) e, futuramente, `OpenAiSpeechAdapter` (Fase 5).
- **`TranscriptionCachePort`** (saída): abstrai o cache. Implementado por `InMemoryCacheAdapter` (Fase 1) e `RedisCacheAdapter` (Fase 2).

### Use Case (`TranscriptionService`)

Fluxo orquestrado:
1. Validação: tamanho (≤5MB), Content-Type permitido, não-nulo
2. `SHA-256(audioBytes)` → chave de cache
3. `cache.findByHash(hash)` → se hit, retorna `transcription.asCached()`
4. `speechToText.transcribe(bytes, filename, contentType)` → nova transcrição
5. `cache.save(hash, transcription)` → armazena
6. Retorna transcrição

Nenhuma importação de classes Spring no use case. Testável com Mockito puro.

### Adapters de saída

**`SpeachesAdapter`** — implementa `SpeechToTextPort`:
- WebClient → `POST /v1/audio/transcriptions` (compatível OpenAI API)
- `MultipartBodyBuilder` com bytes do áudio + model + response_format=json
- Mapeamento `SpeachesResponse → Transcription`
- Erro 4xx/5xx → `SpeechToTextException` com status code
- Timeout configurável via `AppProperties`

**`InMemoryCacheAdapter`** — implementa `TranscriptionCachePort`:
- `ConcurrentHashMap` — sem TTL, sem persistência
- Substituído por `RedisCacheAdapter` na Fase 2 via `@Primary`
- Permanece ativo em testes unitários

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

### Por que portas como interfaces Java e não anotações Spring?

**Decisão:** as portas `SpeechToTextPort` e `TranscriptionCachePort` são interfaces Java puras, sem `@Component` ou qualquer anotação Spring.

**Raciocínio:** se o domínio depende de Spring, ele não pode ser testado sem contexto Spring. Com interfaces puras, `TranscriptionServiceTest` roda em <100ms sem levantar contexto.

**Trade-off:** mais verboso (precisa de `@Component` no adapter). Vale pelo isolamento.

### Por que `InMemoryCacheAdapter` em vez de desabilitar o cache?

**Decisão:** implementar um adapter real com `ConcurrentHashMap` como placeholder.

**Raciocínio:** força o `TranscriptionService` a ser escrito corretamente contra a porta, desde o início. Na Fase 2, basta adicionar `@Primary` no `RedisCacheAdapter` — zero mudança no use case.

**Trade-off:** memória não persistida entre reinicializações em dev. Aceitável para desenvolvimento local.

### Por que SHA-256 e não UUID?

**Decisão:** chave de cache baseada no conteúdo do arquivo (SHA-256), não em um identificador aleatório.

**Raciocínio:** mesmo arquivo enviado duas vezes → mesmo hash → cache hit. Com UUID, dois uploads idênticos gerariam duas chamadas ao Speaches. O hash é idempotente.

**Trade-off:** custo de CPU para calcular SHA-256 a cada request (~0.1ms para 5MB). Desprezível.

### Por que `asCached()` em vez de campo mutável?

**Decisão:** `Transcription` é imutável. `asCached()` retorna nova instância com `cached=true`.

**Raciocínio:** evita efeitos colaterais. O objeto armazenado no cache nunca é modificado.

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
