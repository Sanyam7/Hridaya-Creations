package com.hridayacreations.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A delivery address belonging to a {@link User}. A user may keep multiple addresses and mark one
 * as the default.
 */
@Entity
@Table(name = "addresses")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_address_user"))
    private User user;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "mobile_number", nullable = false, length = 15)
    private String mobileNumber;

    @Column(name = "house_number", length = 100)
    private String houseNumber;

    @Column(name = "street", nullable = false, length = 255)
    private String street;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "pincode", nullable = false, length = 12)
    private String pincode;

    @Builder.Default
    @Column(name = "default_address", nullable = false)
    private boolean defaultAddress = false;
}
