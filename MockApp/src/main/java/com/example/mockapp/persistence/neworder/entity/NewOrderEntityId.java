package com.example.mockapp.persistence.neworder.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class NewOrderEntityId implements Serializable {

    private Long warehouseId;
    private Long districtId;
    private Long orderId;
}
