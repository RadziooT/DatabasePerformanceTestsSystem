package com.example.mockapp.persistence.neworder;

import com.example.mockapp.persistence.neworder.entity.NewOrderEntity;
import com.example.mockapp.persistence.neworder.entity.NewOrderEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewOrderRepository extends JpaRepository<NewOrderEntity, NewOrderEntityId> {

    Optional<NewOrderEntity> findByWarehouseIdAndDistrictIdAndOrderId(Long warehouseId, Long districtId, Long orderId);

    List<NewOrderEntity> findByWarehouseIdAndDistrictIdOrderByOrderIdAsc(Long warehouseId, Long districtId);
}
