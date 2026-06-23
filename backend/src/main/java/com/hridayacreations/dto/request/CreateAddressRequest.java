package com.hridayacreations.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Creates a delivery address for the authenticated user.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CreateAddressRequest", description = "Create address request")
public class CreateAddressRequest {

    @Schema(example = "Aarav Sharma", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name must not exceed 120 characters")
    private String fullName;

    @Schema(example = "9876543210", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile number must be a valid 10-digit Indian number")
    private String mobileNumber;

    @Schema(example = "Flat 4B, Lotus Residency")
    @Size(max = 100, message = "House number must not exceed 100 characters")
    private String houseNumber;

    @Schema(example = "MG Road", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must not exceed 255 characters")
    private String street;

    @Schema(example = "Bengaluru", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Schema(example = "Karnataka", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Schema(example = "India", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Schema(example = "560001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Pincode must be a valid 6-digit Indian pincode")
    private String pincode;

    @Schema(example = "true")
    private Boolean defaultAddress;
}
