package com.hridayacreations.service;

import com.hridayacreations.dto.mapper.OrderMapper;
import com.hridayacreations.dto.request.UpdateOrderStatusRequest;
import com.hridayacreations.entity.Cart;
import com.hridayacreations.entity.Order;
import com.hridayacreations.entity.User;
import com.hridayacreations.entity.enums.OrderStatus;
import com.hridayacreations.exception.BusinessRuleException;
import com.hridayacreations.repository.AddressRepository;
import com.hridayacreations.repository.CartRepository;
import com.hridayacreations.repository.OrderRepository;
import com.hridayacreations.repository.ProductRepository;
import com.hridayacreations.security.services.UserPrincipal;
import com.hridayacreations.service.impl.OrderServiceImpl;
import com.hridayacreations.service.interfaces.AuditLogService;
import com.hridayacreations.service.support.OrderPricingCalculator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderPricingCalculator pricingCalculator;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void authenticate() {
        UserPrincipal principal = new UserPrincipal(1L, "u@example.com", "U E", "pw", true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void placeOrder_noCart_throws() {
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(
                com.hridayacreations.dto.request.CreateOrderRequest.builder().addressId(1L).build()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void placeOrder_emptyCart_throws() {
        Cart cart = Cart.builder().user(User.builder().build()).build();
        when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.placeOrder(
                com.hridayacreations.dto.request.CreateOrderRequest.builder().addressId(1L).build()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void cancelOrder_deliveredOrder_throws() {
        Order order = Order.builder().status(OrderStatus.DELIVERED).build();
        order.setId(5L);
        when(orderRepository.findByIdAndUser_Id(5L, 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(5L, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updateOrderStatus_invalidTransition_throws() {
        Order order = Order.builder().status(OrderStatus.PENDING).build();
        order.setId(6L);
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(6L,
                UpdateOrderStatusRequest.builder().status(OrderStatus.SHIPPED).build()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updateOrderStatus_sameStatus_throws() {
        Order order = Order.builder().status(OrderStatus.PENDING).build();
        order.setId(7L);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(7L,
                UpdateOrderStatusRequest.builder().status(OrderStatus.PENDING).build()))
                .isInstanceOf(BusinessRuleException.class);
    }
}
