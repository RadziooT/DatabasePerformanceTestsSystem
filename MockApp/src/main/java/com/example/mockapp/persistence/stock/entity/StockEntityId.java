package com.example.mockapp.persistence.stock.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StockEntityId implements Serializable {

    private Long warehouseId;
    private Long itemId;
}
