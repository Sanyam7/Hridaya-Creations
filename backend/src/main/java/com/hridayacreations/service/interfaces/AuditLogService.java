package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.response.AuditLogResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.entity.enums.AuditAction;
import org.springframework.data.domain.Pageable;

/**
 * Records and queries business audit events.
 */
public interface AuditLogService {

    /**
     * Persists an audit entry for a significant action. Implementations must be resilient: an audit
     * failure should never break the surrounding business operation.
     *
     * @param action     the audited action
     * @param entityType simple name of the affected entity (e.g. {@code Product})
     * @param entityId   identifier of the affected entity
     * @param details    human-readable context
     */
    void log(AuditAction action, String entityType, String entityId, String details);

    PagedResponse<AuditLogResponse> getLogs(AuditAction action, Pageable pageable);
}
