package com.example.mockapp.domain.order.service;

import com.example.mockapp.api.model.NewOrderRequest;
import com.example.mockapp.api.model.NewOrderResponse;
import com.example.mockapp.domain.customer.model.Customer;
import com.example.mockapp.domain.customer.service.CustomerService;
import com.example.mockapp.domain.district.model.District;
import com.example.mockapp.domain.district.service.DistrictService;
import com.example.mockapp.domain.item.model.Item;
import com.example.mockapp.domain.item.service.ItemService;
import com.example.mockapp.domain.neworder.model.NewOrder;
import com.example.mockapp.domain.neworder.service.NewOrderService;
import com.example.mockapp.domain.order.model.Order;
import com.example.mockapp.domain.orderline.model.OrderLine;
import com.example.mockapp.domain.orderline.service.OrderLineService;
import com.example.mockapp.domain.stock.model.Stock;
import com.example.mockapp.domain.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class NewOrderTransaction {

    private final CustomerService customerService;
    private final DistrictService districtService;
    private final OrderService orderService;
    private final OrderLineService orderLineService;
    private final NewOrderService newOrderService;
    private final StockService stockService;
    private final ItemService itemService;

    @Transactional
    public NewOrderResponse execute(NewOrderRequest request) {
        validateRequest(request);

        Customer customer = customerService.getById(request.getWarehouseId(), request.getDistrictId(), request.getCustomerId());
        District homeDistrict = districtService.getById(request.getWarehouseId(), request.getDistrictId());
        validateDistrictAndWarehouse(request.getDistrictId(), request.getWarehouseId(), homeDistrict);

        LocalDateTime entryDate = request.getEntryDate() != null ? request.getEntryDate() : LocalDateTime.now();
        boolean allLocal = request.getAllLocal() == null || request.getAllLocal();

        Long nextOrderId = districtService.getAndIncrementNextOrderId(request.getWarehouseId(), request.getDistrictId());

        Order createdOrder = orderService.create(Order.builder()
                .warehouseId(customer.getWarehouseId())
                .districtId(customer.getDistrictId())
                .id(nextOrderId)
                .customerId(customer.getId())
                .entryDate(entryDate)
                .orderLineCount(request.getItems().size())
                .allLocal(allLocal)
                .build());

        List<NewOrderResponse.LineItemResponse> lineResponses = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int lineNumber = 1;

        for (NewOrderRequest.LineItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getItemId() == null) {
                throw new IllegalArgumentException("Each order line must include an itemId");
            }
            Item item = itemService.getById(itemRequest.getItemId());
            int quantity = itemRequest.getQuantity() != null ? itemRequest.getQuantity() : 1;
            if (quantity <= 0) {
                throw new IllegalArgumentException("Order line quantity must be positive for itemId=" + item.getId());
            }

            Long supplyWarehouseId = itemRequest.getSupplyWarehouseId() != null
                    ? itemRequest.getSupplyWarehouseId()
                    : createdOrder.getWarehouseId();
            if (!Objects.equals(supplyWarehouseId, createdOrder.getWarehouseId())) {
                allLocal = false;
            }

            Stock stock = stockService.getByWarehouseAndItem(supplyWarehouseId, item.getId());
            int updatedQuantity = stock.getQuantity() - quantity;
            if (updatedQuantity < 10) {
                updatedQuantity += 100;
            }

            BigDecimal quantityValue = BigDecimal.valueOf(quantity);
            BigDecimal lineAmount = item.getPrice().multiply(quantityValue).setScale(2, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(lineAmount);

            stock.setQuantity(updatedQuantity);
            stock.setYearToDate((stock.getYearToDate() == null ? 0 : stock.getYearToDate()) + quantity);
            stock.setOrderCount((stock.getOrderCount() == null ? 0 : stock.getOrderCount()) + 1);
            if (!Objects.equals(supplyWarehouseId, createdOrder.getWarehouseId())) {
                stock.setRemoteCount((stock.getRemoteCount() == null ? 0 : stock.getRemoteCount()) + 1);
            }
            stockService.update(stock.getWarehouseId(), stock.getItemId(), stock);

            OrderLine createdLine = orderLineService.create(OrderLine.builder()
                    .orderId(createdOrder.getId())
                    .districtId(createdOrder.getDistrictId())
                    .warehouseId(createdOrder.getWarehouseId())
                    .lineNumber(lineNumber++)
                    .itemId(item.getId())
                    .supplyWarehouseId(supplyWarehouseId)
                    .quantity(quantity)
                    .amount(lineAmount)
                    .distInfo(item.getData())
                    .build());

            lineResponses.add(NewOrderResponse.LineItemResponse.builder()
                    .lineNumber(createdLine.getLineNumber())
                    .itemId(item.getId())
                    .itemName(item.getName())
                    .supplyWarehouseId(supplyWarehouseId)
                    .quantity(quantity)
                    .unitPrice(item.getPrice())
                    .lineAmount(lineAmount)
                    .stockAfter(updatedQuantity)
                    .build());
        }

        createdOrder = orderService.update(createdOrder.getWarehouseId(), createdOrder.getDistrictId(), createdOrder.getId(), Order.builder()
                .warehouseId(createdOrder.getWarehouseId())
                .districtId(createdOrder.getDistrictId())
                .customerId(createdOrder.getCustomerId())
                .entryDate(createdOrder.getEntryDate())
                .carrierId(createdOrder.getCarrierId())
                .orderLineCount(createdOrder.getOrderLineCount())
                .allLocal(allLocal)
                .build());

        newOrderService.create(NewOrder.builder()
                .warehouseId(createdOrder.getWarehouseId())
                .districtId(createdOrder.getDistrictId())
                .orderId(createdOrder.getId())
                .build());

        return NewOrderResponse.builder()
                .orderId(createdOrder.getId())
                .warehouseId(createdOrder.getWarehouseId())
                .districtId(createdOrder.getDistrictId())
                .customerId(createdOrder.getCustomerId())
                .entryDate(createdOrder.getEntryDate())
                .allLocal(allLocal)
                .orderLineCount(createdOrder.getOrderLineCount())
                .totalAmount(totalAmount)
                .items(lineResponses)
                .build();
    }

    private void validateRequest(NewOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("New order request must not be null");
        }
        if (request.getWarehouseId() == null || request.getDistrictId() == null || request.getCustomerId() == null) {
            throw new IllegalArgumentException("warehouseId, districtId and customerId must not be null");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("New order must contain at least one item");
        }
    }

    private void validateDistrictAndWarehouse(Long requestDistrictId, Long requestWarehouseId, District homeDistrict) {
        if (!Objects.equals(homeDistrict.getId(), requestDistrictId)) {
            throw new IllegalArgumentException("Customer district does not match the requested district");
        }
        if (!Objects.equals(homeDistrict.getWarehouseId(), requestWarehouseId)) {
            throw new IllegalArgumentException("Customer warehouse does not match the requested warehouse");
        }
    }
}

