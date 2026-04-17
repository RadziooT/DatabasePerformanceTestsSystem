package com.example.mockapp.domain.stock.mapper;

import com.example.mockapp.domain.DomainPersistenceMapper;
import com.example.mockapp.domain.stock.model.Stock;
import com.example.mockapp.persistence.stock.entity.StockEntity;
import org.springframework.stereotype.Component;

@Component
public class StockDomainMapper implements DomainPersistenceMapper<Stock, StockEntity> {

    @Override
    public StockEntity toEntity(Stock stock) {
        if (stock == null) {
            return null;
        }
        return StockEntity.builder()
                .warehouseId(stock.getWarehouseId())
                .itemId(stock.getItemId())
                .quantity(stock.getQuantity())
                .district01(stock.getDistrict01())
                .district02(stock.getDistrict02())
                .district03(stock.getDistrict03())
                .district04(stock.getDistrict04())
                .district05(stock.getDistrict05())
                .district06(stock.getDistrict06())
                .district07(stock.getDistrict07())
                .district08(stock.getDistrict08())
                .district09(stock.getDistrict09())
                .district10(stock.getDistrict10())
                .yearToDate(stock.getYearToDate())
                .orderCount(stock.getOrderCount())
                .remoteCount(stock.getRemoteCount())
                .data(stock.getData())
                .build();
    }

    @Override
    public Stock toDomain(StockEntity entity) {
        if (entity == null) {
            return null;
        }
        return Stock.builder()
                .warehouseId(entity.getWarehouseId())
                .itemId(entity.getItemId())
                .quantity(entity.getQuantity())
                .district01(entity.getDistrict01())
                .district02(entity.getDistrict02())
                .district03(entity.getDistrict03())
                .district04(entity.getDistrict04())
                .district05(entity.getDistrict05())
                .district06(entity.getDistrict06())
                .district07(entity.getDistrict07())
                .district08(entity.getDistrict08())
                .district09(entity.getDistrict09())
                .district10(entity.getDistrict10())
                .yearToDate(entity.getYearToDate())
                .orderCount(entity.getOrderCount())
                .remoteCount(entity.getRemoteCount())
                .data(entity.getData())
                .build();
    }
}
