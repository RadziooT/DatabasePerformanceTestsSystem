package com.example.mockapp.domain.orderline.mapper;

import com.example.mockapp.domain.DomainPersistenceMapper;
import com.example.mockapp.domain.orderline.model.OrderLine;
import com.example.mockapp.persistence.orderline.entity.OrderLineEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderLineDomainMapper implements DomainPersistenceMapper<OrderLine, OrderLineEntity> {

    @Override
    public OrderLineEntity toEntity(OrderLine orderLine) {
        if (orderLine == null) {
            return null;
        }
        return OrderLineEntity.builder()
                .orderId(orderLine.getOrderId())
                .districtId(orderLine.getDistrictId())
                .warehouseId(orderLine.getWarehouseId())
                .lineNumber(orderLine.getLineNumber())
                .itemId(orderLine.getItemId())
                .supplyWarehouseId(orderLine.getSupplyWarehouseId())
                .quantity(orderLine.getQuantity())
                .amount(orderLine.getAmount())
                .deliveryDate(orderLine.getDeliveryDate())
                .distInfo(orderLine.getDistInfo())
                .build();
    }

    @Override
    public OrderLine toDomain(OrderLineEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrderLine.builder()
                .orderId(entity.getOrderId())
                .districtId(entity.getDistrictId())
                .warehouseId(entity.getWarehouseId())
                .lineNumber(entity.getLineNumber())
                .itemId(entity.getItemId())
                .supplyWarehouseId(entity.getSupplyWarehouseId())
                .quantity(entity.getQuantity())
                .amount(entity.getAmount())
                .deliveryDate(entity.getDeliveryDate())
                .distInfo(entity.getDistInfo())
                .build();
    }
}
