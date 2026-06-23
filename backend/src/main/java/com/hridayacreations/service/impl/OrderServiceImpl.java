package com.hridayacreations.service.impl;

import com.hridayacreations.dto.mapper.OrderMapper;
import com.hridayacreations.dto.request.CancelOrderRequest;
import com.hridayacreations.dto.request.CreateOrderRequest;
import com.hridayacreations.dto.request.UpdateOrderStatusRequest;
import com.hridayacreations.dto.response.OrderResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.entity.Address;
import com.hridayacreations.entity.Cart;
import com.hridayacreations.entity.CartItem;
import com.hridayacreations.entity.Order;
import com.hridayacreations.entity.OrderItem;
import com.hridayacreations.entity.Product;
import com.hridayacreations.entity.ProductImage;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.enums.AuditAction;
import com.hridayacreations.entity.enums.OrderStatus;
import com.hridayacreations.entity.enums.PaymentStatus;
import com.hridayacreations.entity.enums.ProductStatus;
import com.hridayacreations.exception.BusinessRuleException;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.AddressRepository;
import com.hridayacreations.repository.CartRepository;
import com.hridayacreations.repository.OrderRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.security.SecurityUtils;
import com.hridayacreations.service.interfaces.AuditLogService;
import com.hridayacreations.service.interfaces.OrderService;
import com.hridayacreations.service.support.OrderPricingCalculator;
import com.hridayacreations.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Default order implementation: atomic checkout from the cart with stock decrement and snapshotting,
 * customer cancellation with restock, and admin status transitions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final OrderPricingCalculator pricingCalculator;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new BusinessRuleException("Your cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Your cart is empty");
        }

        Address address = addressRepository.findByIdAndUser_Id(request.getAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));
        User user = cart.getUser();

        Order order = Order.builder()
                .orderNumber(generateUniqueOrderNumber())
                .user(user)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .placedAt(Instant.now())
                .shippingFullName(address.getFullName())
                .shippingMobile(address.getMobileNumber())
                .shippingHouseNumber(address.getHouseNumber())
                .shippingStreet(address.getStreet())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingCountry(address.getCountry())
                .shippingPincode(address.getPincode())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            validatePurchasable(product, cartItem.getQuantity());

            BigDecimal unitPrice = product.getSellingPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .sku(product.getSku())
                    .unitPrice(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .lineTotal(lineTotal)
                    .imageUrl(primaryImageUrl(product))
                    .build();
            order.addItem(orderItem);

            decrementStock(product, cartItem.getQuantity());
        }

        OrderPricingCalculator.PriceBreakdown breakdown = pricingCalculator.calculate(subtotal);
        order.setSubtotal(breakdown.subtotal());
        order.setGstAmount(breakdown.gstAmount());
        order.setDeliveryCharge(breakdown.deliveryCharge());
        order.setTotalAmount(breakdown.totalAmount());

        Order saved = orderRepository.save(order);

        cart.clear();
        cartRepository.save(cart);

        auditLogService.log(AuditAction.ORDER_PLACED, "Order", String.valueOf(saved.getId()),
                "Placed order " + saved.getOrderNumber() + " total " + saved.getTotalAmount());
        log.info("User {} placed order {}", userId, saved.getOrderNumber());
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, CancelOrderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getStatus().isCancellable()) {
            throw new BusinessRuleException("Order in status %s can no longer be cancelled".formatted(order.getStatus()));
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(request != null ? request.getReason() : null);
        restock(order);

        auditLogService.log(AuditAction.ORDER_CANCELLED, "Order", String.valueOf(orderId),
                "Cancelled order " + order.getOrderNumber());
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getMyOrders(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Order> page = orderRepository.findByUser_Id(userId, pageable);
        return PagedResponse.from(page, orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return PagedResponse.from(page, orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus current = order.getStatus();
        OrderStatus target = request.getStatus();
        if (current == target) {
            throw new BusinessRuleException("Order is already in status " + target);
        }
        if (!current.canTransitionTo(target)) {
            throw new BusinessRuleException("Cannot change order status from %s to %s".formatted(current, target));
        }

        if (target == OrderStatus.CANCELLED) {
            restock(order);
        }
        if (target == OrderStatus.DELIVERED) {
            order.setDeliveredAt(Instant.now());
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        order.setStatus(target);

        auditLogService.log(AuditAction.ORDER_STATUS_CHANGED, "Order", String.valueOf(orderId),
                "Status %s -> %s".formatted(current, target));
        return orderMapper.toResponse(orderRepository.save(order));
    }

    /* ----------------------------------------------------------------- */

    private void validatePurchasable(Product product, int quantity) {
        if (product.getProductStatus() != ProductStatus.ACTIVE) {
            throw new BusinessRuleException("Product '%s' is no longer available".formatted(product.getName()));
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
            throw new BusinessRuleException("Insufficient stock for '%s'".formatted(product.getName()));
        }
    }

    private void decrementStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() - quantity);
        if (!product.isInStock() && product.getProductStatus() == ProductStatus.ACTIVE) {
            product.setProductStatus(ProductStatus.OUT_OF_STOCK);
        }
        productRepository.save(product);
    }

    private void restock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null) {
                continue;
            }
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            if (product.getProductStatus() == ProductStatus.OUT_OF_STOCK && product.isInStock()) {
                product.setProductStatus(ProductStatus.ACTIVE);
            }
            productRepository.save(product);
        }
    }

    private String generateUniqueOrderNumber() {
        String orderNumber;
        do {
            orderNumber = ReferenceGenerator.generateOrderNumber();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }

    private String primaryImageUrl(Product product) {
        return product.getImages().stream()
                .filter(ProductImage::isPrimaryImage)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElseGet(() -> product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl());
    }
}
