 package com.example.mockapp.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLevelResponse {

    private Long warehouseId;
    private Long districtId;
    private Integer threshold;
    private Integer recentOrderCount;
    private Integer lowStockItemCount;
    private List<LowStockItemResponse> lowStockItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LowStockItemResponse {
        private Long itemId;
        private Integer quantity;
    }
}
