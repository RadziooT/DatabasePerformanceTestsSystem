package com.example.mockapp.domain.orderline.service;

import com.example.mockapp.common.exception.NotFoundException;
import com.example.mockapp.domain.orderline.mapper.OrderLineDomainMapper;
import com.example.mockapp.domain.orderline.model.OrderLine;
import com.example.mockapp.persistence.orderline.entity.OrderLineEntity;
import com.example.mockapp.persistence.orderline.OrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderLineService {

    private final OrderLineRepository orderLineRepository;
    private final OrderLineDomainMapper mapper;

    public OrderLine create(OrderLine orderLine) {
        OrderLineEntity entity = mapper.toEntity(orderLine);
        OrderLineEntity saved = orderLineRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public List<OrderLine> getByOrderId(Long warehouseId, Long districtId, Long orderId) {
        return orderLineRepository.findByWarehouseIdAndDistrictIdAndOrderIdOrderByLineNumberAsc(warehouseId, districtId, orderId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public List<OrderLine> getByWarehouseDistrictAndOrder(Long warehouseId, Long districtId, Long orderId) {
        return orderLineRepository.findByWarehouseIdAndDistrictIdAndOrderIdOrderByLineNumberAsc(warehouseId, districtId, orderId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public OrderLine update(Long warehouseId, Long districtId, Long orderId, Integer lineNumber, OrderLine updated) {
        OrderLineEntity existing = orderLineRepository.findByWarehouseIdAndDistrictIdAndOrderIdAndLineNumber(warehouseId, districtId, orderId, lineNumber)
                .orElseThrow(() -> new NotFoundException("OrderLine not found with warehouseId=" + warehouseId + ", districtId=" + districtId + ", orderId=" + orderId + " and lineNumber=" + lineNumber));

        existing.setOrderId(updated.getOrderId());
        existing.setDistrictId(updated.getDistrictId());
        existing.setWarehouseId(updated.getWarehouseId());
        existing.setLineNumber(updated.getLineNumber());
        existing.setItemId(updated.getItemId());
        existing.setSupplyWarehouseId(updated.getSupplyWarehouseId());
        existing.setDeliveryDate(updated.getDeliveryDate());
        existing.setQuantity(updated.getQuantity());
        existing.setAmount(updated.getAmount());
        existing.setDistInfo(updated.getDistInfo());

        OrderLineEntity saved = orderLineRepository.save(existing);
        return mapper.toDomain(saved);
    }
}
