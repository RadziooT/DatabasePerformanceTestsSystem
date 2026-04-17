package com.example.mockapp.api.model;

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
public class PaymentRequest {

    private Long warehouseId;
    private Long districtId;
    private Long customerId;
    private Long customerWarehouseId;
    private Long customerDistrictId;
    private String customerLastName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String data;
}
