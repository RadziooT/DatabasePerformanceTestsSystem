package com.example.mockapp.domain.order.service;

import com.example.mockapp.common.exception.NotFoundException;
import com.example.mockapp.domain.order.mapper.OrderDomainMapper;
import com.example.mockapp.domain.order.model.Order;
import com.example.mockapp.persistence.customer.CustomerRepository;
import com.example.mockapp.persistence.customer.entity.CustomerEntity;
import com.example.mockapp.persistence.district.entity.DistrictEntity;
import com.example.mockapp.persistence.order.OrdersRepository;
import com.example.mockapp.persistence.order.entity.OrdersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final OrderDomainMapper mapper;
    private final CustomerRepository customerRepository;

    public Order create(Order order) {
        OrdersEntity entity = mapper.toEntity(order);

        if (entity.getCustomerId() == null) {
            throw new IllegalArgumentException("customerId must not be null when creating an order");
        }

        if (entity.getWarehouseId() == null || entity.getDistrictId() == null) {
            throw new IllegalArgumentException("warehouseId and districtId must not be null when creating an order");
        }

        CustomerEntity customer = customerRepository
                .findByWarehouseIdAndDistrictIdAndId(entity.getWarehouseId(), entity.getDistrictId(), entity.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + entity.getCustomerId()));
        DistrictEntity district = customer.getDistrict();
        if (district == null || district.getWarehouse() == null) {
            throw new IllegalStateException("Customer " + entity.getCustomerId() + " is not associated with a district/warehouse");
        }
        entity.setDistrictId(district.getId());
        entity.setWarehouseId(district.getWarehouse().getId());

        if (entity.getEntryDate() == null) {
            entity.setEntryDate(LocalDateTime.now());
        }
        if (entity.getAllLocal() == null) {
            entity.setAllLocal(1);
        }

        OrdersEntity saved = ordersRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Order getById(Long warehouseId, Long districtId, Long id) {
        OrdersEntity entity = ordersRepository.findByWarehouseIdAndDistrictIdAndId(warehouseId, districtId, id)
                .orElseThrow(() -> new NotFoundException("Order not found with warehouseId=" + warehouseId + ", districtId=" + districtId + " and id=" + id));
        return mapper.toDomain(entity);
    }

    public List<Order> getByCustomerId(Long warehouseId, Long districtId, Long customerId) {
        return ordersRepository.findByWarehouseIdAndDistrictIdAndCustomerId(warehouseId, districtId, customerId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public List<Order> getByWarehouseAndDistrict(Long warehouseId, Long districtId) {
        return ordersRepository.findByWarehouseIdAndDistrictId(warehouseId, districtId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    public Order update(Long warehouseId, Long districtId, Long id, Order updated) {
        OrdersEntity existing = ordersRepository.findByWarehouseIdAndDistrictIdAndId(warehouseId, districtId, id)
                .orElseThrow(() -> new NotFoundException("Order not found with warehouseId=" + warehouseId + ", districtId=" + districtId + " and id=" + id));

        existing.setWarehouseId(updated.getWarehouseId());
        if (updated.getDistrictId() != null) {
            existing.setDistrictId(updated.getDistrictId());
        }
        existing.setCustomerId(updated.getCustomerId());
        existing.setEntryDate(updated.getEntryDate());
        existing.setCarrierId(updated.getCarrierId());
        existing.setOrderLineCount(updated.getOrderLineCount());
        existing.setAllLocal(updated.getAllLocal() != null && !updated.getAllLocal() ? 0 : 1);

        OrdersEntity saved = ordersRepository.save(existing);
        return mapper.toDomain(saved);
    }
}
