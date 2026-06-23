package com.hridayacreations.controller.user;

import com.hridayacreations.constants.MessageConstants;
import com.hridayacreations.dto.request.CreateAddressRequest;
import com.hridayacreations.dto.request.UpdateAddressRequest;
import com.hridayacreations.dto.response.AddressResponse;
import com.hridayacreations.dto.response.ApiResponse;
import com.hridayacreations.service.interfaces.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Authenticated management of the user's delivery addresses.
 */
@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Addresses", description = "Customer delivery address management")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "List the authenticated user's addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ADDRESSES_FETCHED,
                addressService.getMyAddresses()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of the user's addresses by id")
    public ResponseEntity<ApiResponse<AddressResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ADDRESS_FETCHED,
                addressService.getAddress(id)));
    }

    @PostMapping
    @Operation(summary = "Create a delivery address")
    public ResponseEntity<ApiResponse<AddressResponse>> create(@Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MessageConstants.ADDRESS_CREATED, addressService.createAddress(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a delivery address")
    public ResponseEntity<ApiResponse<AddressResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ADDRESS_UPDATED,
                addressService.updateAddress(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a delivery address")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success(MessageConstants.ADDRESS_DELETED));
    }
}
