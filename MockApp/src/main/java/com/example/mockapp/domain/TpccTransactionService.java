package com.example.mockapp.domain;

import com.example.mockapp.api.model.DeliveryRequest;
import com.example.mockapp.api.model.DeliveryResponse;
import com.example.mockapp.api.model.NewOrderRequest;
import com.example.mockapp.api.model.NewOrderResponse;
import com.example.mockapp.api.model.OrderStatusRequest;
import com.example.mockapp.api.model.OrderStatusResponse;
import com.example.mockapp.api.model.PaymentRequest;
import com.example.mockapp.api.model.PaymentResponse;
import com.example.mockapp.api.model.StockLevelRequest;
import com.example.mockapp.api.model.StockLevelResponse;
import com.example.mockapp.domain.customer.model.Customer;
import com.example.mockapp.domain.customer.service.CustomerService;
import com.example.mockapp.domain.district.model.District;
import com.example.mockapp.domain.district.service.DistrictService;
import com.example.mockapp.domain.history.model.History;
import com.example.mockapp.domain.history.service.HistoryService;
import com.example.mockapp.domain.item.model.Item;
import com.example.mockapp.domain.item.service.ItemService;
import com.example.mockapp.domain.neworder.model.NewOrder;
import com.example.mockapp.domain.neworder.service.NewOrderService;
import com.example.mockapp.domain.order.model.Order;
import com.example.mockapp.domain.order.service.OrderService;
import com.example.mockapp.domain.orderline.model.OrderLine;
import com.example.mockapp.domain.orderline.service.OrderLineService;
import com.example.mockapp.domain.stock.model.Stock;
import com.example.mockapp.domain.stock.service.StockService;
import com.example.mockapp.domain.warehouse.model.Warehouse;
import com.example.mockapp.domain.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TpccTransactionService {

    private final CustomerService customerService;
    private final DistrictService districtService;
    private final WarehouseService warehouseService;
    private final OrderService orderService;
    private final OrderLineService orderLineService;
    private final NewOrderService newOrderService;
    private final StockService stockService;
    private final ItemService itemService;
    private final HistoryService historyService;

    @Transactional
    public NewOrderResponse createNewOrder(NewOrderRequest request) {
        validateNewOrderRequest(request);

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

    @Transactional
    public PaymentResponse payment(PaymentRequest request) {
        validatePaymentRequest(request);

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

    @Transactional
    public OrderStatusResponse orderStatus(OrderStatusRequest request) {
        validateOrderStatusRequest(request);

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

    @Transactional
    public DeliveryResponse delivery(DeliveryRequest request) {
        if (request == null || request.getWarehouseId() == null) {
            throw new IllegalArgumentException("warehouseId must not be null");
        }
        if (request.getCarrierId() == null) {
            throw new IllegalArgumentException("carrierId must not be null");
        }

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
            List<NewOrder> openOrders = newOrderService.getByWarehouseAndDistrict(request.getWarehouseId(), districtId);
            NewOrder oldestOpenOrder = openOrders.stream()
                    .min(Comparator.comparing(NewOrder::getOrderId, Comparator.nullsLast(Comparator.naturalOrder())))
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
            for (OrderLine line : orderLines) {
                line.setDeliveryDate(deliveryDate);
                orderLineService.update(line.getWarehouseId(), line.getDistrictId(), line.getOrderId(), line.getLineNumber(), line);
            }

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

    @Transactional
    public StockLevelResponse stockLevel(StockLevelRequest request) {
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
            orderLineService.getByOrderId(order.getWarehouseId(), order.getDistrictId(), order.getId()).forEach(line -> itemIds.add(line.getItemId()));
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

    private void validateNewOrderRequest(NewOrderRequest request) {
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

    private void validatePaymentRequest(PaymentRequest request) {
        if (request == null || request.getWarehouseId() == null || request.getDistrictId() == null) {
            throw new IllegalArgumentException("warehouseId and districtId must not be null");
        }
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    private void validateOrderStatusRequest(OrderStatusRequest request) {
        if (request == null || request.getDistrictId() == null) {
            throw new IllegalArgumentException("districtId must not be null");
        }
        if (request.getCustomerId() == null && request.getCustomerLastName() == null) {
            throw new IllegalArgumentException("Either customerId or customerLastName must be provided");
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
        throw new IllegalArgumentException("Unable to resolve customer from the provided payment/order-status request");
    }
}
