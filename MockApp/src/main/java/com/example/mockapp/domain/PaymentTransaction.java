package com.example.mockapp.domain;

import com.example.mockapp.api.model.PaymentRequest;
import com.example.mockapp.api.model.PaymentResponse;
import com.example.mockapp.domain.customer.model.Customer;
import com.example.mockapp.domain.customer.service.CustomerService;
import com.example.mockapp.domain.district.model.District;
import com.example.mockapp.domain.district.service.DistrictService;
import com.example.mockapp.domain.history.model.History;
import com.example.mockapp.domain.history.service.HistoryService;
import com.example.mockapp.domain.warehouse.model.Warehouse;
import com.example.mockapp.domain.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PaymentTransaction {

    private final CustomerService customerService;
    private final DistrictService districtService;
    private final WarehouseService warehouseService;
    private final HistoryService historyService;

    @Transactional
    public PaymentResponse execute(PaymentRequest request) {
        validateRequest(request);

        Customer customer = resolveCustomer(request.getCustomerId(), request.getCustomerWarehouseId(), request.getCustomerDistrictId(), request.getCustomerLastName(), request.getDistrictId(), request.getWarehouseId());
        District customerDistrict = districtService.getById(customer.getWarehouseId(), customer.getDistrictId());
        if (request.getCustomerWarehouseId() != null && !Objects.equals(customerDistrict.getWarehouseId(), request.getCustomerWarehouseId())) {
            throw new IllegalArgumentException("Customer warehouse does not match the customer district");
        }

        Warehouse terminalWarehouse = warehouseService.applyPayment(request.getWarehouseId(), request.getAmount());
        District terminalDistrict = districtService.applyPayment(request.getWarehouseId(), request.getDistrictId(), request.getAmount());
        Customer updatedCustomer = customerService.applyPayment(customer.getWarehouseId(), customer.getDistrictId(), customer.getId(), request.getAmount());

        LocalDateTime paymentDate = request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now();
        String historyData = request.getData() != null ? request.getData() : "TPC-C payment";
        History history = historyService.create(History.builder()
                .customerId(updatedCustomer.getId())
                .customerDistrictId(customerDistrict.getId())
                .customerWarehouseId(customerDistrict.getWarehouseId())
                .districtId(terminalDistrict.getId())
                .warehouseId(terminalWarehouse.getId())
                .date(paymentDate)
                .amount(request.getAmount())
                .data(historyData)
                .build());

        return PaymentResponse.builder()
                .warehouseId(terminalWarehouse.getId())
                .districtId(terminalDistrict.getId())
                .customerId(updatedCustomer.getId())
                .customerFirstName(updatedCustomer.getFirstName())
                .customerLastName(updatedCustomer.getLastName())
                .amount(request.getAmount())
                .customerBalance(updatedCustomer.getBalance())
                .customerPaymentCount(updatedCustomer.getPaymentCount())
                .warehouseYearToDate(terminalWarehouse.getYearToDate())
                .districtYearToDate(terminalDistrict.getYearToDate())
                .historyId(history.getId())
                .paymentDate(paymentDate)
                .historyData(history.getData())
                .build();
    }

    private void validateRequest(PaymentRequest request) {
        if (request == null || request.getWarehouseId() == null || request.getDistrictId() == null) {
            throw new IllegalArgumentException("warehouseId and districtId must not be null");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    private Customer resolveCustomer(Long customerId, Long customerWarehouseId, Long customerDistrictId, String customerLastName, Long fallbackDistrictId, Long fallbackWarehouseId) {
        Long warehouseIdToUse = customerWarehouseId != null ? customerWarehouseId : fallbackWarehouseId;
        Long districtIdToUse = customerDistrictId != null ? customerDistrictId : fallbackDistrictId;

        if (customerId != null) {
            if (warehouseIdToUse == null || districtIdToUse == null) {
                throw new IllegalArgumentException("districtId must be provided when resolving customer by id");
            }
            return customerService.getById(warehouseIdToUse, districtIdToUse, customerId);
        }
        if (warehouseIdToUse != null && districtIdToUse != null && customerLastName != null) {
            return customerService.getByDistrictAndLastName(warehouseIdToUse, districtIdToUse, customerLastName);
        }
        throw new IllegalArgumentException("Unable to resolve customer from the provided payment request");
    }
}

