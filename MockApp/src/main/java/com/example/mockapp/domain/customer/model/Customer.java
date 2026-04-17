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
    private String email;
    private BigDecimal balance;
    private Integer paymentCount;
}
