package com.example.mockapp.domain.warehouse.mapper;

import com.example.mockapp.domain.DomainPersistenceMapper;
import com.example.mockapp.domain.warehouse.model.Warehouse;
import com.example.mockapp.persistence.warehouse.entity.WarehouseEntity;
import org.springframework.stereotype.Component;

@Component
public class WarehouseDomainMapper implements DomainPersistenceMapper<Warehouse, WarehouseEntity> {

    @Override
    public WarehouseEntity toEntity(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        return WarehouseEntity.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .street1(warehouse.getStreet1())
                .street2(warehouse.getStreet2())
                .city(warehouse.getCity())
                .state(warehouse.getState())
                .zip(warehouse.getZip())
                .tax(warehouse.getTax())
                .yearToDate(warehouse.getYearToDate())
                .build();
    }

    @Override
    public Warehouse toDomain(WarehouseEntity entity) {
        if (entity == null) {
            return null;
        }
        return Warehouse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .street1(entity.getStreet1())
                .street2(entity.getStreet2())
                .city(entity.getCity())
                .state(entity.getState())
                .zip(entity.getZip())
                .tax(entity.getTax())
                .yearToDate(entity.getYearToDate())
                .build();
    }
}
