package com.example.mockapp.persistence.order;

import com.example.mockapp.persistence.order.entity.OrdersEntity;
import com.example.mockapp.persistence.order.entity.OrdersEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<OrdersEntity, OrdersEntityId> {

    Optional<OrdersEntity> findByWarehouseIdAndDistrictIdAndId(Long warehouseId, Long districtId, Long id);

    Optional<OrdersEntity> findTopByWarehouseIdAndDistrictIdOrderByIdDesc(Long warehouseId, Long districtId);

    List<OrdersEntity> findByWarehouseIdAndDistrictIdAndCustomerId(Long warehouseId, Long districtId, Long customerId);

    List<OrdersEntity> findByWarehouseIdAndDistrictId(Long warehouseId, Long districtId);
}
