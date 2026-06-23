package com.hridayacreations.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Optional reason supplied by a customer when cancelling their order.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CancelOrderRequest", description = "Cancel order request")
public class CancelOrderRequest {

    @Schema(example = "Ordered by mistake")
    @Size(max = 500, message = "Cancellation reason must not exceed 500 characters")
    private String reason;
}
