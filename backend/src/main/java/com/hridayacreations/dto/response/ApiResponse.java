package com.hridayacreations.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Standard envelope returned by every endpoint.
 *
 * <pre>
 * {
 *   "success": true,
 *   "message": "Product created successfully",
 *   "data": { ... },
 *   "timestamp": "2026-06-22T10:15:30Z"
 * }
 * </pre>
 *
 * @param <T> type of the {@code data} payload
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ApiResponse", description = "Standard API response envelope")
public class ApiResponse<T> {

    @Schema(description = "Indicates whether the request was processed successfully", example = "true")
    private boolean success;

    @Schema(description = "Human-readable message describing the result", example = "Operation completed successfully")
    private String message;

    @Schema(description = "Response payload; may be null for operations without a body")
    private T data;

    @Schema(description = "Server timestamp (UTC) when the response was generated")
    private Instant timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
