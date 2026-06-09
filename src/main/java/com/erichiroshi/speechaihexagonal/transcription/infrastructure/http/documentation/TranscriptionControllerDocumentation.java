package com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.documentation;

import com.erichiroshi.speechaihexagonal.transcription.infrastructure.http.response.TranscriptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * Contrato de documentação OpenAPI para o {@code TranscriptionController}.
 *
 * <p>Padrão arquitetural: as anotações Swagger ficam nesta interface,
 * mantendo o controller focado exclusivamente na responsabilidade de adaptação HTTP.
 * Isso reduz acoplamento com a ferramenta de documentação e facilita leitura do controller.
 */
@Tag(name = "Transcription", description = "Transcrição de áudio via Speaches (Whisper local) ou OpenAI Whisper-1")
public interface TranscriptionControllerDocumentation {

    @Operation(
            summary = "Transcrever áudio",
            description = """
                    Transcreve um arquivo de áudio para texto.
                    
                    **Deduplicação automática:** se o mesmo arquivo já foi transcrito anteriormente,
                    o resultado é retornado instantaneamente do cache Redis ou PostgreSQL,
                    sem nova chamada ao motor de transcrição.
                    
                    **Provider selecionado via** `speech.provider`:
                    - `speaches` (padrão) → Whisper local, gratuito, requer GPU
                    - `openai` → Whisper-1 cloud, pago, sem infra local
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Transcrição realizada ou recuperada com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TranscriptionResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                              "audioHash": "a3f2c1d4e5b6...",
                              "audioTranscription": "Olá, este é o texto transcrito do áudio.",
                              "createdAt": "2025-01-01T10:00:00"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Arquivo ausente, vazio, maior que 5MB ou Content-Type inválido",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(value = """
                            {
                              "type": "/errors/audio-validation",
                              "title": "Arquivo de áudio inválido",
                              "status": 400,
                              "detail": "Content-Type não suportado. Tipos aceitos: [audio/wav, audio/mpeg, ...]",
                              "field": "file"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "429",
            description = "Muitas requisições simultâneas — Bulkhead cheio",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    )
    @ApiResponse(
            responseCode = "502",
            description = "Falha no motor de transcrição (Speaches/OpenAI indisponível)",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    )
    @ApiResponse(
            responseCode = "503",
            description = "Circuit Breaker OPEN — serviço temporariamente indisponível",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    )
    ResponseEntity<TranscriptionResponse> transcription(
            @Parameter(
                    description = "Arquivo de áudio a transcrever. Tipos aceitos: wav, mp3, mp4, webm, ogg. Tamanho máximo: 5MB.",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            MultipartFile file
    );
}