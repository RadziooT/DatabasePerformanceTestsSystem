package com.example.mockapp.persistence.customer;

import com.example.mockapp.persistence.customer.entity.CustomerEntity;
import com.example.mockapp.persistence.customer.entity.CustomerEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<CustomerEntity, CustomerEntityId> {

    Optional<CustomerEntity> findByWarehouseIdAndDistrictIdAndId(Long warehouseId, Long districtId, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CustomerEntity c where c.warehouseId = :warehouseId and c.districtId = :districtId and c.id = :id")
    Optional<CustomerEntity> findByWarehouseIdAndDistrictIdAndIdForUpdate(@Param("warehouseId") Long warehouseId, @Param("districtId") Long districtId, @Param("id") Long id);

    Optional<CustomerEntity> findTopByWarehouseIdAndDistrictIdOrderByIdDesc(Long warehouseId, Long districtId);

    List<CustomerEntity> findByWarehouseIdAndDistrictIdAndLastNameOrderByFirstNameAscIdAsc(Long warehouseId, Long districtId, String lastName);
}
