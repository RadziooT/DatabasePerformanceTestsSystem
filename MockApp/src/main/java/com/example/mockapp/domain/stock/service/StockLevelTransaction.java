package com.example.mockapp.domain.stock.service;

import com.example.mockapp.api.model.StockLevelRequest;
import com.example.mockapp.api.model.StockLevelResponse;
import com.example.mockapp.domain.order.model.Order;
import com.example.mockapp.domain.order.service.OrderService;
import com.example.mockapp.domain.orderline.service.OrderLineService;
import com.example.mockapp.domain.stock.model.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class StockLevelTransaction {

    private final OrderService orderService;
    private final OrderLineService orderLineService;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public StockLevelResponse execute(StockLevelRequest request) {
        if (request == null || request.getWarehouseId() == null || request.getDistrictId() == null) {
            throw new IllegalArgumentException("warehouseId and districtId must not be null");
        }

        int threshold = request.getThreshold() != null ? request.getThreshold() : 10;
        int recentOrderCount = request.getRecentOrderCount() != null ? request.getRecentOrderCount() : 20;

        List<Order> recentOrders = orderService.getByWarehouseAndDistrict(request.getWarehouseId(), request.getDistrictId()).stream()
                .sorted(Comparator.comparing(Order::getEntryDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Order::getId, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .limit(recentOrderCount)
                .toList();

        Set<Long> itemIds = new LinkedHashSet<>();
        for (Order order : recentOrders) {
            orderLineService.getByOrderId(order.getWarehouseId(), order.getDistrictId(), order.getId())
                    .forEach(line -> itemIds.add(line.getItemId()));
        }

        List<StockLevelResponse.LowStockItemResponse> lowStockItems = new ArrayList<>();
        for (Long itemId : itemIds) {
            Stock stock = stockService.getByWarehouseAndItem(request.getWarehouseId(), itemId);
            if (stock.getQuantity() != null && stock.getQuantity() < threshold) {
                lowStockItems.add(StockLevelResponse.LowStockItemResponse.builder()
                        .itemId(itemId)
                        .quantity(stock.getQuantity())
                        .build());
            }
        }

        return StockLevelResponse.builder()
                .warehouseId(request.getWarehouseId())
                .districtId(request.getDistrictId())
                .threshold(threshold)
                .recentOrderCount(recentOrderCount)
                .lowStockItemCount(lowStockItems.size())
                .lowStockItems(lowStockItems)
                .build();
    }
}

