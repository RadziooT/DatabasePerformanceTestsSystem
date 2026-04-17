package com.example.mockapp.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLevelRequest {

    private Long warehouseId;
    private Long districtId;
    private Integer threshold;
    private Integer recentOrderCount;
}
