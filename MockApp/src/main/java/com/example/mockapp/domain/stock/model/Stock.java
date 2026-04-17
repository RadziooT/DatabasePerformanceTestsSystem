package com.example.mockapp.domain.stock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    private Long warehouseId;
    private Long itemId;
    private Integer quantity;
    private String district01;
    private String district02;
    private String district03;
    private String district04;
    private String district05;
    private String district06;
    private String district07;
    private String district08;
    private String district09;
    private String district10;
    private Integer yearToDate;
    private Integer orderCount;
    private Integer remoteCount;
    private String data;
}
