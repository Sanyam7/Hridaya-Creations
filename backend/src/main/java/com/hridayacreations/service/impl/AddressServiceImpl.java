package com.hridayacreations.service.impl;

import com.hridayacreations.dto.mapper.AddressMapper;
import com.hridayacreations.dto.request.CreateAddressRequest;
import com.hridayacreations.dto.request.UpdateAddressRequest;
import com.hridayacreations.dto.response.AddressResponse;
import com.hridayacreations.entity.Address;
import com.hridayacreations.entity.User;
import com.hridayacreations.exception.ResourceNotFoundException;
import com.hridayacreations.repository.AddressRepository;
import com.hridayacreations.repository.UserRepository;
import com.hridayacreations.security.SecurityUtils;
import com.hridayacreations.service.interfaces.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default address management. Maintains the single-default-address invariant per user.
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponse createAddress(CreateAddressRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = getUser(userId);

        Address address = addressMapper.toEntity(request);
        address.setUser(user);

        boolean shouldBeDefault = Boolean.TRUE.equals(request.getDefaultAddress())
                || addressRepository.countByUser_Id(userId) == 0;
        if (shouldBeDefault) {
            addressRepository.clearDefaultForUser(userId);
        }
        address.setDefaultAddress(shouldBeDefault);

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, UpdateAddressRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Address address = getOwnedAddress(addressId, userId);

        addressMapper.updateEntity(request, address);
        if (Boolean.TRUE.equals(request.getDefaultAddress())) {
            addressRepository.clearDefaultForUser(userId);
            address.setDefaultAddress(true);
        } else if (request.getDefaultAddress() != null) {
            address.setDefaultAddress(false);
        }

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Address address = getOwnedAddress(addressId, userId);
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);

        if (wasDefault) {
            addressRepository.findByUser_IdOrderByDefaultAddressDescIdDesc(userId).stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setDefaultAddress(true);
                        addressRepository.save(next);
                    });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddress(Long addressId) {
        return addressMapper.toResponse(getOwnedAddress(addressId, SecurityUtils.getCurrentUserId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses() {
        Long userId = SecurityUtils.getCurrentUserId();
        return addressRepository.findByUser_IdOrderByDefaultAddressDescIdDesc(userId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private Address getOwnedAddress(Long addressId, Long userId) {
        return addressRepository.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
    }
}
