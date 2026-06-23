package com.hridayacreations.service.interfaces;

import com.hridayacreations.dto.request.CreateAddressRequest;
import com.hridayacreations.dto.request.UpdateAddressRequest;
import com.hridayacreations.dto.response.AddressResponse;

import java.util.List;

/**
 * Management of the authenticated user's delivery addresses.
 */
public interface AddressService {

    AddressResponse createAddress(CreateAddressRequest request);

    AddressResponse updateAddress(Long addressId, UpdateAddressRequest request);

    void deleteAddress(Long addressId);

    AddressResponse getAddress(Long addressId);

    List<AddressResponse> getMyAddresses();
}
