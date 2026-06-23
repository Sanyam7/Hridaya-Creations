package com.hridayacreations.dto.request;

import com.hridayacreations.entity.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Places an order from the authenticated user's cart, shipped to one of their saved addresses.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateOrderRequest", description = "Place-order request")
public class CreateOrderRequest {

    @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED,
            description = "Id of a delivery address owned by the user")
    @NotNull(message = "Address id is required")
    private Long addressId;

    @Schema(example = "CASH_ON_DELIVERY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Schema(example = "Please deliver after 6 PM")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
