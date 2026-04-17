package com.example.mockapp.domain.neworder.mapper;

import com.example.mockapp.domain.DomainPersistenceMapper;
import com.example.mockapp.domain.neworder.model.NewOrder;
import com.example.mockapp.persistence.neworder.entity.NewOrderEntity;
import org.springframework.stereotype.Component;

@Component
public class NewOrderDomainMapper implements DomainPersistenceMapper<NewOrder, NewOrderEntity> {

    @Override
    public NewOrderEntity toEntity(NewOrder newOrder) {
        if (newOrder == null) {
            return null;
        }
        return NewOrderEntity.builder()
                .warehouseId(newOrder.getWarehouseId())
                .districtId(newOrder.getDistrictId())
                .orderId(newOrder.getOrderId())
                .build();
    }

    @Override
    public NewOrder toDomain(NewOrderEntity entity) {
        if (entity == null) {
            return null;
        }
        return NewOrder.builder()
                .warehouseId(entity.getWarehouseId())
                .districtId(entity.getDistrictId())
                .orderId(entity.getOrderId())
                .build();
    }
}
