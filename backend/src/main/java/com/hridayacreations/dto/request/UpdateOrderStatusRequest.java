package com.hridayacreations.dto.request;

import com.hridayacreations.entity.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin payload to transition an order to a new status.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UpdateOrderStatusRequest", description = "Update order status request")
public class UpdateOrderStatusRequest {

    @Schema(example = "CONFIRMED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Order status is required")
    private OrderStatus status;
}
