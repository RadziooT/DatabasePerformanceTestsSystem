package com.example.mockapp.domain.neworder.service;

import com.example.mockapp.common.exception.NotFoundException;
import com.example.mockapp.domain.neworder.mapper.NewOrderDomainMapper;
import com.example.mockapp.domain.neworder.model.NewOrder;
import com.example.mockapp.persistence.neworder.NewOrderRepository;
import com.example.mockapp.persistence.neworder.entity.NewOrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewOrderService {

    private final NewOrderRepository newOrderRepository;
    private final NewOrderDomainMapper mapper;

    public NewOrder create(NewOrder newOrder) {
        NewOrderEntity entity = mapper.toEntity(newOrder);
        NewOrderEntity saved = newOrderRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Optional<NewOrder> getOldestByWarehouseAndDistrictForUpdate(Long warehouseId, Long districtId) {
        return newOrderRepository.findTopByWarehouseIdAndDistrictIdOrderByOrderIdAsc(warehouseId, districtId)
                .map(mapper::toDomain);
    }

    public void delete(Long warehouseId, Long districtId, Long orderId) {
        NewOrderEntity existing = newOrderRepository.findByWarehouseIdAndDistrictIdAndOrderId(warehouseId, districtId, orderId)
                .orElseThrow(() -> new NotFoundException("NewOrder not found with warehouseId=" + warehouseId + ", districtId=" + districtId + " and orderId=" + orderId));
        newOrderRepository.delete(existing);
    }
}
