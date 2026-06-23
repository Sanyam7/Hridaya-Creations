package com.hridayacreations.entity.enums;

import java.util.Set;

/**
 * Order lifecycle states and the allowed forward transitions between them.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /**
     * Returns the set of statuses this status is permitted to transition into.
     *
     * @return allowed next statuses (empty for terminal states)
     */
    public Set<OrderStatus> allowedTransitions() {
        return switch (this) {
            case PENDING -> Set.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> Set.of(PROCESSING, CANCELLED);
            case PROCESSING -> Set.of(SHIPPED, CANCELLED);
            case SHIPPED -> Set.of(DELIVERED);
            case DELIVERED, CANCELLED -> Set.of();
        };
    }

    /**
     * @param target candidate next status
     * @return {@code true} if a transition from this status to {@code target} is allowed
     */
    public boolean canTransitionTo(OrderStatus target) {
        return allowedTransitions().contains(target);
    }

    /**
     * @return {@code true} if the order may still be cancelled by the customer
     */
    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED || this == PROCESSING;
    }
}
