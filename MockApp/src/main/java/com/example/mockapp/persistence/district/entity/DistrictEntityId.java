package com.example.mockapp.persistence.district.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DistrictEntityId implements Serializable {

    private Long warehouseId;
    private Long id;
}
