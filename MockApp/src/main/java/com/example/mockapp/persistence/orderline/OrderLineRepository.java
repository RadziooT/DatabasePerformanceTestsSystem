package com.example.mockapp.persistence.orderline;

import com.example.mockapp.persistence.orderline.entity.OrderLineEntity;
import com.example.mockapp.persistence.orderline.entity.OrderLineEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderLineRepository extends JpaRepository<OrderLineEntity, OrderLineEntityId> {

    List<OrderLineEntity> findByWarehouseIdAndDistrictIdAndOrderIdOrderByLineNumberAsc(Long warehouseId, Long districtId, Long orderId);

    Optional<OrderLineEntity> findByWarehouseIdAndDistrictIdAndOrderIdAndLineNumber(Long warehouseId, Long districtId, Long orderId, Integer lineNumber);
}
