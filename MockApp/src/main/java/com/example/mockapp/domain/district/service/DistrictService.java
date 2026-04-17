package com.example.mockapp.domain.district.service;

import com.example.mockapp.common.exception.NotFoundException;
import com.example.mockapp.domain.district.mapper.DistrictDomainMapper;
import com.example.mockapp.domain.district.model.District;
import com.example.mockapp.persistence.district.entity.DistrictEntity;
import com.example.mockapp.persistence.district.DistrictRepository;
import com.example.mockapp.persistence.order.OrderRepository;
import com.example.mockapp.persistence.warehouse.entity.WarehouseEntity;
import com.example.mockapp.persistence.warehouse.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final OrderRepository orderRepository;
    private final WarehouseRepository warehouseRepository;
    private final DistrictDomainMapper mapper;

    public District create(District district, Long warehouseId) {
        WarehouseEntity warehouseEntity = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + warehouseId));

        DistrictEntity entity = mapper.toEntity(district, warehouseEntity);
        if (entity.getId() == null) {
            Long nextDistrictId = districtRepository.findTopByWarehouseIdOrderByIdDesc(warehouseId)
                    .map(found -> found.getId() + 1)
                    .orElse(1L);
            entity.setId(nextDistrictId);
        }
        entity.setWarehouseId(warehouseId);
        if (entity.getNextOrderId() == null) {
            entity.setNextOrderId(1L);
        }
        DistrictEntity saved = districtRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public District getById(Long warehouseId, Long districtId) {
        DistrictEntity entity = districtRepository.findByWarehouseIdAndId(warehouseId, districtId)
                .orElseThrow(() -> new NotFoundException("District not found with warehouseId=" + warehouseId + " and id=" + districtId));
        return mapper.toDomain(entity);
    }

    public List<District> getAll() {
        return districtRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    public District applyPayment(Long warehouseId, Long districtId, BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Payment amount must not be null");
        }

        DistrictEntity existing = districtRepository.findByWarehouseIdAndId(warehouseId, districtId)
                .orElseThrow(() -> new NotFoundException("District not found with warehouseId=" + warehouseId + " and id=" + districtId));

        BigDecimal currentYtd = existing.getYearToDate() != null ? existing.getYearToDate() : BigDecimal.ZERO;
        existing.setYearToDate(currentYtd.add(amount));

        DistrictEntity saved = districtRepository.save(existing);
        return mapper.toDomain(saved);
    }

    @Transactional
    public Long getAndIncrementNextOrderId(Long warehouseId, Long districtId) {
        DistrictEntity existing = districtRepository.findByWarehouseIdAndIdForUpdate(warehouseId, districtId)
                .orElseThrow(() -> new NotFoundException("District not found with warehouseId=" + warehouseId + " and id=" + districtId));

        Long currentNextOrderId = existing.getNextOrderId() == null ? 1L : existing.getNextOrderId();
        Long minAvailableOrderId = orderRepository.findTopByWarehouseIdAndDistrictIdOrderByIdDesc(warehouseId, districtId)
                .map(order -> order.getId() + 1)
                .orElse(1L);
        Long allocatedOrderId = Math.max(currentNextOrderId, minAvailableOrderId);

        // Keep the district counter in sync even if seed data drifted.
        existing.setNextOrderId(allocatedOrderId + 1);
        districtRepository.save(existing);

        return allocatedOrderId;
    }
}
