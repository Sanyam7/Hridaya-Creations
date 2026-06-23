package com.hridayacreations.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;

import java.time.Instant;
import java.util.List;

/**
 * Error envelope returned for any non-2xx outcome. Keeps the same {@code success}/{@code message}/
 * {@code timestamp} shape as {@link ApiResponse} and adds error diagnostics.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Standard error response envelope")
public class ErrorResponse {

    @Schema(description = "Always false for error responses", example = "false")
    @Builder.Default
    private boolean success = false;

    @Schema(description = "Summary error message", example = "Validation failed")
    private String message;

    @Schema(description = "Machine-readable error code", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Request path that produced the error", example = "/api/v1/products")
    private String path;

    @Schema(description = "Field-level validation errors, when applicable")
    @Singular("fieldError")
    private List<FieldValidationError> errors;

    @Schema(description = "Server timestamp (UTC)")
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * A single field validation failure.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "FieldValidationError", description = "Validation failure for a single field")
    public static class FieldValidationError {
        @Schema(description = "Name of the rejected field", example = "email")
        private String field;
        @Schema(description = "Rejected value")
        private Object rejectedValue;
        @Schema(description = "Validation message", example = "must be a valid email")
        private String message;
    }
}
