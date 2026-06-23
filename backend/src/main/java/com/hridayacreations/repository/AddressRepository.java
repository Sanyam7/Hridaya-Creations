package com.hridayacreations.repository;

import com.hridayacreations.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser_IdOrderByDefaultAddressDescIdDesc(Long userId);

    Optional<Address> findByIdAndUser_Id(Long id, Long userId);

    Optional<Address> findByUser_IdAndDefaultAddressTrue(Long userId);

    long countByUser_Id(Long userId);

    @Modifying
    @Query("UPDATE Address a SET a.defaultAddress = false WHERE a.user.id = :userId")
    void clearDefaultForUser(@Param("userId") Long userId);
}
