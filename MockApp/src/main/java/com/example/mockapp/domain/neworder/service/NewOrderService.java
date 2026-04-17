package com.example.mockapp.domain.neworder.service;

import com.example.mockapp.common.exception.NotFoundException;
import com.example.mockapp.domain.neworder.mapper.NewOrderDomainMapper;
import com.example.mockapp.domain.neworder.model.NewOrder;
import com.example.mockapp.persistence.neworder.entity.NewOrderEntity;
import com.example.mockapp.persistence.neworder.NewOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<NewOrder> getByWarehouseAndDistrict(Long warehouseId, Long districtId) {
        return newOrderRepository.findByWarehouseIdAndDistrictIdOrderByOrderIdAsc(warehouseId, districtId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public void delete(Long warehouseId, Long districtId, Long orderId) {
        NewOrderEntity existing = newOrderRepository.findByWarehouseIdAndDistrictIdAndOrderId(warehouseId, districtId, orderId)
                .orElseThrow(() -> new NotFoundException("NewOrder not found with warehouseId=" + warehouseId + ", districtId=" + districtId + " and orderId=" + orderId));
        newOrderRepository.delete(existing);
    }
}
