package com.example.mockapp.persistence.order;

import com.example.mockapp.persistence.order.entity.OrderEntity;
import com.example.mockapp.persistence.order.entity.OrderEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, OrderEntityId> {

    Optional<OrderEntity> findByWarehouseIdAndDistrictIdAndId(Long warehouseId, Long districtId, Long orderId);

    Optional<OrderEntity> findTopByWarehouseIdAndDistrictIdOrderByIdDesc(Long warehouseId, Long districtId);

    List<OrderEntity> findByWarehouseIdAndDistrictIdAndCustomerId(Long warehouseId, Long districtId, Long customerId);

    List<OrderEntity> findByWarehouseIdAndDistrictId(Long warehouseId, Long districtId);
}
