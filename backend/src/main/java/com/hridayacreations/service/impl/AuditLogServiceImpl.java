package com.hridayacreations.service.impl;

import com.hridayacreations.constants.AppConstants;
import com.hridayacreations.dto.response.AuditLogResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.entity.AuditLog;
import com.hridayacreations.entity.enums.AuditAction;
import com.hridayacreations.repository.AuditLogRepository;
import com.hridayacreations.security.SecurityUtils;
import com.hridayacreations.service.interfaces.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Default audit log implementation. Each entry is written in a separate transaction so audit
 * persistence is decoupled from the business transaction, and any failure is swallowed and logged.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String entityType, String entityId, String details) {
        try {
            AuditLog entry = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .performedBy(resolveActor())
                    .details(details)
                    .createdAt(Instant.now())
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Failed to persist audit log for action {} on {} {}: {}",
                    action, entityType, entityId, ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getLogs(AuditAction action, Pageable pageable) {
        Page<AuditLog> page = action == null
                ? auditLogRepository.findAll(pageable)
                : auditLogRepository.findByAction(action, pageable);
        return PagedResponse.from(page, this::toResponse);
    }

    private String resolveActor() {
        try {
            return SecurityUtils.getCurrentUserEmail();
        } catch (Exception ex) {
            return AppConstants.SYSTEM_AUDITOR;
        }
    }

    private AuditLogResponse toResponse(AuditLog entry) {
        return AuditLogResponse.builder()
                .id(entry.getId())
                .action(entry.getAction())
                .entityType(entry.getEntityType())
                .entityId(entry.getEntityId())
                .performedBy(entry.getPerformedBy())
                .details(entry.getDetails())
                .ipAddress(entry.getIpAddress())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
