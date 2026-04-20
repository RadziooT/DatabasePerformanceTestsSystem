package com.example.mockapp.persistence.customer;

import com.example.mockapp.persistence.customer.entity.CustomerEntity;
import com.example.mockapp.persistence.customer.entity.CustomerEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, CustomerEntityId> {

    Optional<CustomerEntity> findByWarehouseIdAndDistrictIdAndId(Long warehouseId, Long districtId, Long id);

    Optional<CustomerEntity> findTopByWarehouseIdAndDistrictIdOrderByIdDesc(Long warehouseId, Long districtId);

    List<CustomerEntity> findByWarehouseIdAndDistrictIdAndLastNameOrderByFirstNameAscIdAsc(Long warehouseId, Long districtId, String lastName);
}
