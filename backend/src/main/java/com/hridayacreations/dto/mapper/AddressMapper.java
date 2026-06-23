package com.hridayacreations.dto.mapper;

import com.hridayacreations.dto.request.CreateAddressRequest;
import com.hridayacreations.dto.request.UpdateAddressRequest;
import com.hridayacreations.dto.response.AddressResponse;
import com.hridayacreations.entity.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Maps between {@link Address} and its request/response DTOs. The owning user and the default-flag
 * are managed explicitly by the service (the latter to maintain a single default per user).
 */
@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressResponse toResponse(Address address);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "defaultAddress", ignore = true)
    Address toEntity(CreateAddressRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "defaultAddress", ignore = true)
    void updateEntity(UpdateAddressRequest request, @MappingTarget Address address);
}
