package com.hridayacreations.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Representation of a saved delivery address.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AddressResponse", description = "Delivery address details")
public class AddressResponse {

    private Long id;
    private String fullName;
    private String mobileNumber;
    private String houseNumber;
    private String street;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private boolean defaultAddress;
    private Instant createdAt;
    private Instant updatedAt;
}
