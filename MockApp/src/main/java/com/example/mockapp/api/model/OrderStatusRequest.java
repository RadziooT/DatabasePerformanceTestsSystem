package com.example.mockapp.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusRequest {

    private Long warehouseId;
    private Long districtId;
    private Long customerId;
    private Long customerWarehouseId;
    private Long customerDistrictId;
    private String customerLastName;
}
