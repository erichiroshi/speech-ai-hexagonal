package com.erichiroshi.speechaihexagonal.analysis.infrastructure.http.documentation;

import com.erichiroshi.speechaihexagonal.analysis.infrastructure.http.response.AnalysisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Contrato de documentação OpenAPI para o {@code AnalysisController}.
 *
 * <p>Anotações Swagger isoladas nesta interface — o controller permanece limpo.
 */
@Tag(name = "Analysis", description = "Geração de resumos via LLM local (Ollama)")
public interface AnalysisControllerDocumentation {

    @Operation(
            summary = "Gerar ou recuperar resumo de transcrição",
            description = """
                    Gera um resumo conciso da transcrição identificada pelo audioHash,
                    utilizando o modelo LLM configurado via Ollama.
                    
                    **Cache inteligente:**
                    - **1ª chamada:** ~5–30s (geração pelo LLM) → `cached: false`
                    - **Chamadas seguintes:** ~15ms (Redis hit) → `cached: true`
                    
                    **Pré-requisito:** o áudio deve ter sido previamente transcrito via
                    `POST /api/transcriptions`.
                    """
    )

    @ApiResponse(
            responseCode = "200",
            description = "Resumo gerado ou recuperado com sucesso",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = AnalysisResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "id": "7ab12c3d-1234-5678-abcd-ef0123456789",
                              "audioHash": "a3f2c1d4e5b6...",
                              "summary": "O áudio aborda os principais tópicos sobre...",
                              "model": "llama3.2:1b",
                              "cached": false,
                              "createdAt": "2025-01-01T10:00:05Z"
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Transcrição não encontrada para o audioHash informado",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(value = """
                            {
                              "type": "/errors/transcription-not-found",
                              "title": "Transcrição não encontrada",
                              "status": 404,
                              "detail": "Transcrição não encontrada para audioHash: a3f2c1...",
                              "audioHash": "a3f2c1..."
                            }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "503",
            description = "Modelo LLM (Ollama) temporariamente indisponível",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)
    )
    ResponseEntity<AnalysisResponse> analyze(
            @Parameter(
                    description = "SHA-256 hexadecimal (64 caracteres) do áudio original, retornado pelo endpoint de transcrição.",
                    example = "a3f2c1d4e5b6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2",
                    required = true
            )
            String audioHash
    );
}