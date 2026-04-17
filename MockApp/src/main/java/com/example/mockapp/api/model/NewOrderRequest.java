package com.example.mockapp.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewOrderRequest {

    private Long warehouseId;
    private Long districtId;
    private Long customerId;
    private LocalDateTime entryDate;
    @Builder.Default
    private Boolean allLocal = Boolean.TRUE;
    private List<LineItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineItemRequest {
        private Long itemId;
        private Long supplyWarehouseId;
        private Integer quantity;
    }
}
