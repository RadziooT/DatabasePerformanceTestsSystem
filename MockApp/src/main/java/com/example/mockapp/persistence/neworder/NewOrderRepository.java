package com.example.mockapp.persistence.neworder;

import com.example.mockapp.persistence.neworder.entity.NewOrderEntity;
import com.example.mockapp.persistence.neworder.entity.NewOrderEntityId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface NewOrderRepository extends JpaRepository<NewOrderEntity, NewOrderEntityId> {

    List<NewOrderEntity> findByWarehouseIdAndDistrictIdOrderByOrderIdAsc(Long warehouseId, Long districtId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<NewOrderEntity> findTopByWarehouseIdAndDistrictIdOrderByOrderIdAsc(Long warehouseId, Long districtId);

    Optional<NewOrderEntity> findByWarehouseIdAndDistrictIdAndOrderId(Long warehouseId, Long districtId, Long orderId);
}
