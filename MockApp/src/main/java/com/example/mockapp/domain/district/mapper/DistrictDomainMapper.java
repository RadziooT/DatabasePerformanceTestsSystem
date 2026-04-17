package com.example.mockapp.domain.district.mapper;

import com.example.mockapp.domain.DomainPersistenceMapper;
import com.example.mockapp.domain.district.model.District;
import com.example.mockapp.persistence.district.entity.DistrictEntity;
import com.example.mockapp.persistence.warehouse.entity.WarehouseEntity;
import org.springframework.stereotype.Component;

@Component
public class DistrictDomainMapper implements DomainPersistenceMapper<District, DistrictEntity> {

    public DistrictEntity toEntity(District district, WarehouseEntity warehouse) {
        if (district == null && warehouse == null) {
            return null;
        }
        DistrictEntity.DistrictEntityBuilder builder = DistrictEntity.builder();
        if (district != null) {
            builder
                    .id(district.getId())
                    .name(district.getName())
                    .street1(district.getStreet1())
                    .street2(district.getStreet2())
                    .city(district.getCity())
                    .state(district.getState())
                    .zip(district.getZip())
                    .tax(district.getTax())
                    .yearToDate(district.getYearToDate())
                    .nextOrderId(district.getNextOrderId());
        }
        if (warehouse != null) {
            builder.warehouseId(warehouse.getId());
            builder.warehouse(warehouse);
        }
        return builder.build();
    }

    @Override
    public DistrictEntity toEntity(District district) {
        // This method should be used only when the warehouse is already set on the DistrictEntity
        // or warehouse association is handled elsewhere.
        return toEntity(district, null);
    }

    @Override
    public District toDomain(DistrictEntity entity) {
        if (entity == null) {
            return null;
        }
        return District.builder()
                .id(entity.getId())
                .warehouseId(entity.getWarehouseId())
                .name(entity.getName())
                .street1(entity.getStreet1())
                .street2(entity.getStreet2())
                .city(entity.getCity())
                .state(entity.getState())
                .zip(entity.getZip())
                .tax(entity.getTax())
                .yearToDate(entity.getYearToDate())
                .nextOrderId(entity.getNextOrderId())
                .build();
    }
}

