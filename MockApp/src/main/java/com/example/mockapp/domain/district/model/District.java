package com.example.mockapp.domain.district.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class District {

    private Long id;
    private Long warehouseId;
    private String name;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private BigDecimal tax;
    private BigDecimal yearToDate;
    private Long nextOrderId;
}
