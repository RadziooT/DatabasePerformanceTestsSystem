package com.example.mockapp.persistence.order.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderEntityId implements Serializable {

    private Long warehouseId;
    private Long districtId;
    private Long id;
}
