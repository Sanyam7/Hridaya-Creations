package com.hridayacreations.controller.admin;

import com.hridayacreations.constants.AppConstants;
import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.UpdateOrderStatusRequest;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.dto.response.OrderResponse;
import com.hridayacreations.dto.response.PagedResponse;
import com.hridayacreations.entity.enums.OrderStatus;
import com.hridayacreations.service.interfaces.OrderService;
import com.hridayacreations.util.PageableBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin order management: list all orders, view details, and transition status.
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Orders", description = "Administrative order management (ADMIN only)")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "List all orders, optionally filtered by status (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        Pageable pageable = PageableBuilder.build(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ORDERS_FETCHED,
                orderService.getAllOrders(status, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get any order by id")
    public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ORDER_FETCHED, orderService.getOrderById(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update an order's status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(@PathVariable Long id,
                                                                   @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ORDER_STATUS_UPDATED,
                orderService.updateOrderStatus(id, request)));
    }
}
