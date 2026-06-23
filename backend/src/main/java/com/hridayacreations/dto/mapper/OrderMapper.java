package com.hridayacreations.dto.mapper;

import com.hridayacreations.dto.response.OrderItemResponse;
import com.hridayacreations.dto.response.OrderResponse;
import com.hridayacreations.entity.Order;
import com.hridayacreations.entity.OrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Maps {@link Order}/{@link OrderItem} entities to their response DTOs, reconstructing the
 * snapshotted shipping address and item count.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "shippingAddress", ignore = true)
    @Mapping(target = "totalItems", ignore = true)
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    OrderItemResponse toItemResponse(OrderItem item);

    @AfterMapping
    default void enrich(Order order, @MappingTarget OrderResponse.OrderResponseBuilder builder) {
        builder.totalItems(order.getItems() == null ? 0 : order.getItems().size());
        builder.shippingAddress(OrderResponse.ShippingAddressResponse.builder()
                .fullName(order.getShippingFullName())
                .mobileNumber(order.getShippingMobile())
                .houseNumber(order.getShippingHouseNumber())
                .street(order.getShippingStreet())
                .city(order.getShippingCity())
                .state(order.getShippingState())
                .country(order.getShippingCountry())
                .pincode(order.getShippingPincode())
                .build());
    }
}
