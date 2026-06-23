package com.hridayacreations.entity;

import com.hridayacreations.entity.enums.OrderStatus;
import com.hridayacreations.entity.enums.PaymentMethod;
import com.hridayacreations.entity.enums.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A customer order. Monetary fields and the shipping address are snapshotted at placement time so
 * that later catalog or address changes do not alter historical orders.
 */
@Entity
@Table(
        name = "orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_number", columnNames = "order_number"),
        indexes = {
                @Index(name = "idx_order_user", columnList = "user_id"),
                @Index(name = "idx_order_status", columnList = "status")
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, length = 40)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_user"))
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod = PaymentMethod.CASH_ON_DELIVERY;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "gst_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "delivery_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryCharge;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "placed_at")
    private Instant placedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    /* ----- Shipping address snapshot ----- */
    @Column(name = "shipping_full_name", nullable = false, length = 120)
    private String shippingFullName;

    @Column(name = "shipping_mobile", nullable = false, length = 15)
    private String shippingMobile;

    @Column(name = "shipping_house_number", length = 100)
    private String shippingHouseNumber;

    @Column(name = "shipping_street", nullable = false, length = 255)
    private String shippingStreet;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String shippingCity;

    @Column(name = "shipping_state", nullable = false, length = 100)
    private String shippingState;

    @Column(name = "shipping_country", nullable = false, length = 100)
    private String shippingCountry;

    @Column(name = "shipping_pincode", nullable = false, length = 12)
    private String shippingPincode;

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
