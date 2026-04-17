package com.example.mockapp.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusResponse {

    private Long customerId;
    private Long districtId;
    private Long warehouseId;
    private String customerFirstName;
    private String customerLastName;
    private BigDecimal customerBalance;
    private Integer customerPaymentCount;
    private Long latestOrderId;
    private LocalDateTime latestOrderDate;
    private Long latestOrderCarrierId;
    private Integer latestOrderLineCount;
    private List<OrderLineResponse> orderLines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderLineResponse {
        private Integer lineNumber;
        private Long itemId;
        private Long supplyWarehouseId;
        private Integer quantity;
        private BigDecimal amount;
        private LocalDateTime deliveryDate;
    }
}
