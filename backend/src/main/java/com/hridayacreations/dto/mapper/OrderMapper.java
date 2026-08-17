package com.hridayacreations.dto.mapper;

import com.hridayacreations.dto.response.CustomizationValueResponse;
import com.hridayacreations.dto.response.OrderItemResponse;
import com.hridayacreations.dto.response.OrderResponse;
import com.hridayacreations.entity.CustomizationEntry;
import com.hridayacreations.entity.Order;
import com.hridayacreations.entity.OrderItem;
import com.hridayacreations.util.CustomizationValues;
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

    /**
     * Renders a purchased line's customization from its own snapshot — label, field type and typed
     * value all come from what was captured at checkout, never from the product's configuration as
     * it stands today.
     */
    default CustomizationValueResponse toCustomizationResponse(CustomizationEntry entry) {
        if (entry == null) {
            return null;
        }
        return CustomizationValueResponse.builder()
                .key(entry.getOptionKey())
                .label(entry.getLabel())
                .fieldType(entry.getFieldType())
                .value(CustomizationValues.toApi(entry.getFieldType(), entry.getValue()))
                .build();
    }

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
