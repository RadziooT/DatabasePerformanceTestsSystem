package com.example.mockapp.domain.neworder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewOrder {

    private Long warehouseId;
    private Long districtId;
    private Long orderId;
}
