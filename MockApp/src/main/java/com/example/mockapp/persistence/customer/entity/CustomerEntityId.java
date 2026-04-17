package com.example.mockapp.persistence.customer.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CustomerEntityId implements Serializable {

    private Long warehouseId;
    private Long districtId;
    private Long id;
}
