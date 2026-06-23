package com.hridayacreations.controller.order;

import com.hridayacreations.constants.AppConstants;
import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.CancelOrderRequest;
import com.hridayacreations.dto.request.CreateOrderRequest;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.OrderResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.service.interfaces.OrderService;
import com.hridayacreations.util.PageableBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated customer order placement and tracking.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Customer order placement and tracking")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place an order from the cart")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.ORDER_PLACED, orderService.placeOrder(request)));
    }

    @GetMapping
    @Operation(summary = "List the authenticated user's orders (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> myOrders(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        Pageable pageable = PageableBuilder.build(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ORDERS_FETCHED,
                orderService.getMyOrders(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of the user's orders by id")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ORDER_FETCHED, orderService.getMyOrder(id)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel one of the user's orders")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id,
                                                                  @RequestBody(required = false) CancelOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ORDER_CANCELLED,
                orderService.cancelOrder(id, request)));
    }
}
