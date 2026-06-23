package com.hridayacreations.dto.request;

import com.hridayacreations.validator.FieldMatch;
import com.hridayacreations.validator.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for self-service customer registration.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldMatch(first = "password", second = "confirmPassword",
        message = "Password and confirm password must match")
@Schema(name = "RegisterRequest", description = "Customer registration request")
public class RegisterRequest {

    @Schema(example = "Aarav", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(max = 60, message = "First name must not exceed 60 characters")
    private String firstName;

    @Schema(example = "Sharma", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(max = 60, message = "Last name must not exceed 60 characters")
    private String lastName;

    @Schema(example = "aarav.sharma@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    @Schema(example = "9876543210", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Mobile number must be a valid 10-digit Indian number")
    private String mobileNumber;

    @Schema(example = "Secret@123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @StrongPassword
    private String password;

    @Schema(example = "Secret@123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
