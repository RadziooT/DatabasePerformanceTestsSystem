package com.example.mockapp.domain.orderline.model;

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
public class OrderLine {

    private Long orderId;
    private Long districtId;
    private Long warehouseId;
    private Integer lineNumber;
    private Long itemId;
    private Long supplyWarehouseId;
    private LocalDateTime deliveryDate;
    private Integer quantity;
    private BigDecimal amount;
    private String distInfo;
}
