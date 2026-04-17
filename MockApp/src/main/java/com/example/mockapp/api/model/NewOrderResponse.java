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
public class NewOrderResponse {

    private Long orderId;
    private Long warehouseId;
    private Long districtId;
    private Long customerId;
    private LocalDateTime entryDate;
    private Boolean allLocal;
    private Integer orderLineCount;
    private BigDecimal totalAmount;
    private List<LineItemResponse> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItemResponse {
        private Integer lineNumber;
        private Long itemId;
        private String itemName;
        private Long supplyWarehouseId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineAmount;
        private Integer stockAfter;
    }
}
