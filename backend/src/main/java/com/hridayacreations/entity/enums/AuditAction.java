package com.hridayacreations.entity.enums;

/**
 * Business actions captured by the audit logging subsystem.
 */
public enum AuditAction {
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED,
    CATEGORY_CREATED,
    CATEGORY_UPDATED,
    CATEGORY_DELETED,
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    ORDER_PLACED,
    ORDER_STATUS_CHANGED,
    ORDER_CANCELLED,
    IMAGE_UPLOADED,
    IMAGE_DELETED,
    PRICING_UPDATED,
    INVENTORY_UPDATED
}
