package com.hridayacreations.controller.admin;

import com.hridayacreations.constants.AppConstants;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.AuditLogResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.entity.enums.AuditAction;
import com.hridayacreations.service.interfaces.AuditLogService;
import com.hridayacreations.util.PageableBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin access to the audit trail.
 */
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Audit Logs", description = "Administrative audit trail (ADMIN only)")
public class AdminAuditController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "List audit logs, optionally filtered by action (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> list(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        Pageable pageable = PageableBuilder.build(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully",
                auditLogService.getLogs(action, pageable)));
    }
}
