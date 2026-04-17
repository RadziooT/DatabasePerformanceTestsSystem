package com.example.mockapp.persistence.orderline.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderLineEntityId implements Serializable {

    private Long warehouseId;
    private Long districtId;
    private Long orderId;
    private Integer lineNumber;
}
