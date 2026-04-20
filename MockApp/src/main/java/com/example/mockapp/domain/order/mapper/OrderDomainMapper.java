package com.example.mockapp.domain.order.mapper;

import com.example.mockapp.domain.DomainPersistenceMapper;
import com.example.mockapp.domain.order.model.Order;
import com.example.mockapp.persistence.order.entity.OrdersEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderDomainMapper implements DomainPersistenceMapper<Order, OrdersEntity> {

    @Override
    public OrdersEntity toEntity(Order order) {
        if (order == null) {
            return null;
        }
        return OrdersEntity.builder()
                .id(order.getId())
                .warehouseId(order.getWarehouseId())
                .districtId(order.getDistrictId())
                .customerId(order.getCustomerId())
                .entryDate(order.getEntryDate())
                .carrierId(order.getCarrierId())
                .orderLineCount(order.getOrderLineCount())
                .allLocal(Boolean.FALSE.equals(order.getAllLocal()) ? 0 : 1)
                .build();
    }

    @Override
    public Order toDomain(OrdersEntity entity) {
        if (entity == null) {
            return null;
        }
        return Order.builder()
                .id(entity.getId())
                .warehouseId(entity.getWarehouseId())
                .districtId(entity.getDistrictId())
                .customerId(entity.getCustomerId())
                .entryDate(entity.getEntryDate())
                .carrierId(entity.getCarrierId())
                .orderLineCount(entity.getOrderLineCount())
                .allLocal(entity.getAllLocal() != null && entity.getAllLocal() == 1)
                .build();
    }
}
