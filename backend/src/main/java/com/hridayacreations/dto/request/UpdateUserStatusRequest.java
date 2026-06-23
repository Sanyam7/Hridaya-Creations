package com.hridayacreations.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Admin payload to enable or disable a user account.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UpdateUserStatusRequest", description = "Enable/disable a user account")
public class UpdateUserStatusRequest {

    @Schema(example = "false", requiredMode = Schema.RequiredMode.REQUIRED,
            description = "true to enable the account, false to disable")
    @NotNull(message = "Enabled flag is required")
    private Boolean enabled;
}
