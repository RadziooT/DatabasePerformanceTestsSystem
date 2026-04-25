package com.example.mockapp.domain.customer.service;

import com.example.mockapp.common.exception.NotFoundException;
import com.example.mockapp.domain.customer.mapper.CustomerDomainMapper;
import com.example.mockapp.domain.customer.model.Customer;
import com.example.mockapp.persistence.customer.CustomerRepository;
import com.example.mockapp.persistence.customer.entity.CustomerEntity;
import com.example.mockapp.persistence.district.DistrictRepository;
import com.example.mockapp.persistence.district.entity.DistrictEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final DistrictRepository districtRepository;
    private final CustomerDomainMapper mapper;

    public Customer create(Customer customer, Long warehouseId, Long districtId) {
        DistrictEntity districtEntity = districtRepository.findByWarehouseIdAndId(warehouseId, districtId)
                .orElseThrow(() -> new NotFoundException("District not found with warehouseId=" + warehouseId + " and id=" + districtId));

        CustomerEntity entity = mapper.toEntity(customer, districtEntity);
        if (entity.getId() == null) {
            Long nextCustomerId = customerRepository.findTopByWarehouseIdAndDistrictIdOrderByIdDesc(warehouseId, districtId)
                    .map(found -> found.getId() + 1)
                    .orElse(1L);
            entity.setId(nextCustomerId);
        }
        entity.setWarehouseId(warehouseId);
        entity.setDistrictId(districtId);
        if (entity.getBalance() == null) {
            entity.setBalance(BigDecimal.ZERO);
        }
        if (entity.getYearToDatePayment() == null) {
            entity.setYearToDatePayment(BigDecimal.ZERO);
        }
        if (entity.getPaymentCount() == null) {
            entity.setPaymentCount(0);
        }
        if (entity.getDeliveryCount() == null) {
            entity.setDeliveryCount(0);
        }
        CustomerEntity saved = customerRepository.save(entity);
        return mapper.toDomain(saved);
    }

    public Customer getById(Long warehouseId, Long districtId, Long id) {
        CustomerEntity entity = customerRepository.findByWarehouseIdAndDistrictIdAndId(warehouseId, districtId, id)
                .orElseThrow(() -> new NotFoundException("Customer not found with warehouseId=" + warehouseId + ", districtId=" + districtId + " and id=" + id));
        return mapper.toDomain(entity);
    }

    public Customer getByDistrictAndLastName(Long warehouseId, Long districtId, String lastName) {
        List<Customer> matches = customerRepository
                .findByWarehouseIdAndDistrictIdAndLastNameOrderByFirstNameAscIdAsc(warehouseId, districtId, lastName).stream()
                .map(mapper::toDomain)
                .toList();

        if (matches.isEmpty()) {
            throw new NotFoundException("Customer not found for warehouseId=" + warehouseId + ", districtId=" + districtId + " and lastName=" + lastName);
        }

        return matches.get((matches.size() - 1) / 2);
    }

    public Customer applyPayment(Long warehouseId, Long districtId, Long id, BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Payment amount must not be null");
        }

        CustomerEntity existingCustomer = customerRepository.findByWarehouseIdAndDistrictIdAndIdForUpdate(warehouseId, districtId, id)
                .orElseThrow(() -> new NotFoundException("Customer not found with warehouseId=" + warehouseId + ", districtId=" + districtId + " and id=" + id));

        BigDecimal currentBalance = existingCustomer.getBalance() != null ? existingCustomer.getBalance() : BigDecimal.ZERO;
        Integer currentCount = existingCustomer.getPaymentCount() != null ? existingCustomer.getPaymentCount() : 0;
        BigDecimal currentYtd = existingCustomer.getYearToDatePayment() != null ? existingCustomer.getYearToDatePayment() : BigDecimal.ZERO;

        existingCustomer.setBalance(currentBalance.subtract(amount));
        existingCustomer.setPaymentCount(currentCount + 1);
        existingCustomer.setYearToDatePayment(currentYtd.add(amount));

        CustomerEntity saved = customerRepository.save(existingCustomer);
        return mapper.toDomain(saved);
    }

    public Customer addDeliveryAndApplyOrderAmount(Long warehouseId, Long districtId, Long id, BigDecimal orderAmount) {
        CustomerEntity existingCustomer = customerRepository.findByWarehouseIdAndDistrictIdAndId(warehouseId, districtId, id)
                .orElseThrow(() -> new NotFoundException("Customer not found with warehouseId=" + warehouseId + ", districtId=" + districtId + " and id=" + id));

        BigDecimal currentBalance = existingCustomer.getBalance() != null ? existingCustomer.getBalance() : BigDecimal.ZERO;
        Integer currentDeliveryCount = existingCustomer.getDeliveryCount() != null ? existingCustomer.getDeliveryCount() : 0;

        existingCustomer.setBalance(currentBalance.add(orderAmount));
        existingCustomer.setDeliveryCount(currentDeliveryCount + 1);

        CustomerEntity saved = customerRepository.save(existingCustomer);
        return mapper.toDomain(saved);
    }
}
