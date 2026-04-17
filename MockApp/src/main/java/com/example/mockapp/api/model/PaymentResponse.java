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
public class PaymentResponse {

    private Long warehouseId;
    private Long districtId;
    private Long customerId;
    private String customerFirstName;
    private String customerLastName;
    private BigDecimal amount;
    private BigDecimal customerBalance;
    private Integer customerPaymentCount;
    private BigDecimal warehouseYearToDate;
    private BigDecimal districtYearToDate;
    private Long historyId;
    private LocalDateTime paymentDate;
    private String historyData;
}
