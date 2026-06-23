package com.hridayacreations.dto.response;

import com.hridayacreations.entity.enums.AuditAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Representation of an audit log entry (admin-facing).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AuditLogResponse", description = "Audit log entry")
public class AuditLogResponse {

    private Long id;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String performedBy;
    private String details;
    private String ipAddress;
    private Instant createdAt;
}
