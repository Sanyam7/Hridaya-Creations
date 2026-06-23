package com.hridayacreations.dto.request;

import com.hridayacreations.validator.FieldMatch;
import com.hridayacreations.validator.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Changes the authenticated user's password after verifying the current one.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldMatch(first = "newPassword", second = "confirmPassword",
        message = "New password and confirm password must match")
@Schema(name = "ChangePasswordRequest", description = "Change-password request")
public class ChangePasswordRequest {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(example = "NewSecret@123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New password is required")
    @StrongPassword
    private String newPassword;

    @Schema(example = "NewSecret@123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
