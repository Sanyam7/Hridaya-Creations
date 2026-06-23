package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.request.CancelOrderRequest;
import com.hridayacreations.dto.request.CreateOrderRequest;
import com.hridayacreations.dto.request.UpdateOrderStatusRequest;
import com.hridayacreations.dto.response.OrderResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.entity.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

/**
 * Order placement, tracking and administration.
 */
public interface OrderService {

    /* ----- Customer ----- */

    OrderResponse placeOrder(CreateOrderRequest request);

    OrderResponse cancelOrder(Long orderId, CancelOrderRequest request);

    OrderResponse getMyOrder(Long orderId);

    PagedResponse<OrderResponse> getMyOrders(Pageable pageable);

    /* ----- Admin ----- */

    PagedResponse<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable);

    OrderResponse getOrderById(Long orderId);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}
