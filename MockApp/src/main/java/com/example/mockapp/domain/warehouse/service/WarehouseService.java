package com.example.mockapp.domain.warehouse.service;

import com.example.mockapp.common.exception.NotFoundException;
import com.example.mockapp.domain.warehouse.mapper.WarehouseDomainMapper;
import com.example.mockapp.domain.warehouse.model.Warehouse;
import com.example.mockapp.persistence.warehouse.WarehouseRepository;
import com.example.mockapp.persistence.warehouse.entity.WarehouseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseDomainMapper mapper;

    public Warehouse create(Warehouse warehouse) {
        WarehouseEntity entity = mapper.toEntity(warehouse);
        WarehouseEntity saved = warehouseRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Warehouse applyPayment(Long id, BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Payment amount must not be null");
        }

        WarehouseEntity existing = warehouseRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + id));

        BigDecimal currentYtd = existing.getYearToDate() != null ? existing.getYearToDate() : BigDecimal.ZERO;
        existing.setYearToDate(currentYtd.add(amount));

        WarehouseEntity saved = warehouseRepository.save(existing);
        return mapper.toDomain(saved);
    }
}
