package com.example.mockapp.domain.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    private Long id;
    private Long warehouseId;
    private Long districtId;
    private Long customerId;
    private LocalDateTime entryDate;
    private Long carrierId;
    private Integer orderLineCount;
    private Boolean allLocal;
}
