package com.example.mockapp.domain;

import com.example.mockapp.api.model.DeliveryRequest;
import com.example.mockapp.api.model.DeliveryResponse;
import com.example.mockapp.domain.customer.service.CustomerService;
import com.example.mockapp.domain.district.model.District;
import com.example.mockapp.domain.district.service.DistrictService;
import com.example.mockapp.domain.neworder.model.NewOrder;
import com.example.mockapp.domain.neworder.service.NewOrderService;
import com.example.mockapp.domain.order.model.Order;
import com.example.mockapp.domain.order.service.OrderService;
import com.example.mockapp.domain.orderline.model.OrderLine;
import com.example.mockapp.domain.orderline.service.OrderLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DeliveryTransaction {

    private final DistrictService districtService;
    private final OrderService orderService;
    private final OrderLineService orderLineService;
    private final NewOrderService newOrderService;
    private final CustomerService customerService;

    @Transactional
    public DeliveryResponse execute(DeliveryRequest request) {
        validateRequest(request);

        LocalDateTime deliveryDate = request.getDeliveryDate() != null ? request.getDeliveryDate() : LocalDateTime.now();
        List<Long> districtIds = request.getDistrictIds();
        if (districtIds == null || districtIds.isEmpty()) {
            districtIds = districtService.getAll().stream()
                    .filter(district -> Objects.equals(district.getWarehouseId(), request.getWarehouseId()))
                    .map(District::getId)
                    .toList();
        }

        List<DeliveryResponse.DeliveredOrderResponse> deliveredOrders = new ArrayList<>();
        for (Long districtId : districtIds) {
            NewOrder oldestOpenOrder = newOrderService
                    .getOldestByWarehouseAndDistrictForUpdate(request.getWarehouseId(), districtId)
                    .orElse(null);

            if (oldestOpenOrder == null) {
                continue;
            }

            Order order = orderService.getById(oldestOpenOrder.getWarehouseId(), oldestOpenOrder.getDistrictId(), oldestOpenOrder.getOrderId());

            orderService.update(order.getWarehouseId(), order.getDistrictId(), order.getId(), Order.builder()
                    .warehouseId(order.getWarehouseId())
                    .districtId(order.getDistrictId())
                    .customerId(order.getCustomerId())
                    .entryDate(order.getEntryDate())
                    .carrierId(request.getCarrierId())
                    .orderLineCount(order.getOrderLineCount())
                    .allLocal(order.getAllLocal())
                    .build());

            List<OrderLine> orderLines = orderLineService.getByWarehouseDistrictAndOrder(request.getWarehouseId(), districtId, order.getId());
            BigDecimal orderTotal = BigDecimal.ZERO;
            for (OrderLine line : orderLines) {
                line.setDeliveryDate(deliveryDate);
                orderLineService.update(line.getWarehouseId(), line.getDistrictId(), line.getOrderId(), line.getLineNumber(), line);
                orderTotal = orderTotal.add(line.getAmount() != null ? line.getAmount() : BigDecimal.ZERO);
            }

            customerService.addDeliveryAndApplyOrderAmount(order.getWarehouseId(), order.getDistrictId(), order.getCustomerId(), orderTotal);
            newOrderService.delete(oldestOpenOrder.getWarehouseId(), oldestOpenOrder.getDistrictId(), oldestOpenOrder.getOrderId());

            deliveredOrders.add(DeliveryResponse.DeliveredOrderResponse.builder()
                    .districtId(districtId)
                    .orderId(order.getId())
                    .customerId(order.getCustomerId())
                    .lineCount(orderLines.size())
                    .build());
        }

        return DeliveryResponse.builder()
                .warehouseId(request.getWarehouseId())
                .carrierId(request.getCarrierId())
                .deliveryDate(deliveryDate)
                .deliveredOrders(deliveredOrders)
                .build();
    }

    private void validateRequest(DeliveryRequest request) {
        if (request == null || request.getWarehouseId() == null) {
            throw new IllegalArgumentException("warehouseId must not be null");
        }
        if (request.getCarrierId() == null) {
            throw new IllegalArgumentException("carrierId must not be null");
        }
    }
}

