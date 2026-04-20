package com.example.mockapp.domain.order.service;

import com.example.mockapp.api.model.OrderStatusRequest;
import com.example.mockapp.api.model.OrderStatusResponse;
import com.example.mockapp.domain.customer.model.Customer;
import com.example.mockapp.domain.customer.service.CustomerService;
import com.example.mockapp.domain.district.model.District;
import com.example.mockapp.domain.district.service.DistrictService;
import com.example.mockapp.domain.order.model.Order;
import com.example.mockapp.domain.orderline.service.OrderLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderStatusTransaction {

    private final CustomerService customerService;
    private final DistrictService districtService;
    private final OrderService orderService;
    private final OrderLineService orderLineService;

    @Transactional(readOnly = true)
    public OrderStatusResponse execute(OrderStatusRequest request) {
        validateRequest(request);

        Customer customer = resolveCustomer(request.getCustomerId(), request.getCustomerWarehouseId(), request.getCustomerDistrictId(), request.getCustomerLastName(), request.getDistrictId(), request.getWarehouseId());
        District customerDistrict = districtService.getById(customer.getWarehouseId(), customer.getDistrictId());
        List<Order> customerOrders = orderService.getByCustomerId(customer.getWarehouseId(), customer.getDistrictId(), customer.getId());

        Order latestOrder = customerOrders.stream()
                .max(Comparator.comparing(Order::getEntryDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Order::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);

        List<OrderStatusResponse.OrderLineResponse> orderLines = new ArrayList<>();
        if (latestOrder != null) {
            orderLines = orderLineService.getByOrderId(latestOrder.getWarehouseId(), latestOrder.getDistrictId(), latestOrder.getId()).stream()
                    .map(line -> OrderStatusResponse.OrderLineResponse.builder()
                            .lineNumber(line.getLineNumber())
                            .itemId(line.getItemId())
                            .supplyWarehouseId(line.getSupplyWarehouseId())
                            .quantity(line.getQuantity())
                            .amount(line.getAmount())
                            .deliveryDate(line.getDeliveryDate())
                            .build())
                    .toList();
        }

        return OrderStatusResponse.builder()
                .customerId(customer.getId())
                .districtId(customerDistrict.getId())
                .warehouseId(customerDistrict.getWarehouseId())
                .customerFirstName(customer.getFirstName())
                .customerLastName(customer.getLastName())
                .customerBalance(customer.getBalance())
                .customerPaymentCount(customer.getPaymentCount())
                .latestOrderId(latestOrder != null ? latestOrder.getId() : null)
                .latestOrderDate(latestOrder != null ? latestOrder.getEntryDate() : null)
                .latestOrderCarrierId(latestOrder != null ? latestOrder.getCarrierId() : null)
                .latestOrderLineCount(latestOrder != null ? latestOrder.getOrderLineCount() : null)
                .orderLines(orderLines)
                .build();
    }

    private void validateRequest(OrderStatusRequest request) {
        if (request == null || request.getDistrictId() == null) {
            throw new IllegalArgumentException("districtId must not be null");
        }
        if (request.getCustomerId() == null && request.getCustomerLastName() == null) {
            throw new IllegalArgumentException("Either customerId or customerLastName must be provided");
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
        throw new IllegalArgumentException("Unable to resolve customer from the provided order-status request");
    }
}

