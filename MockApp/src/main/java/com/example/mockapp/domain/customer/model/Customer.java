package com.example.mockapp.domain.customer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    private Long id;
    private Long warehouseId;
    private Long districtId;
    private String firstName;
    private String lastName;
    private BigDecimal balance;
    private BigDecimal yearToDatePayment;
    private Integer paymentCount;
    private Integer deliveryCount;
    private String credit;
    private BigDecimal creditLimit;
    private BigDecimal discount;
    private String data;
}
