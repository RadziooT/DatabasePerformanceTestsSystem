package com.example.mockapp.domain.history.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class History {

    private Long id;
    private Long customerId;
    private Long customerDistrictId;
    private Long customerWarehouseId;
    private Long districtId;
    private Long warehouseId;
    private LocalDateTime date;
    private BigDecimal amount;
    private String data;
}
