package com.example.mockapp.domain.stock.service;

import com.example.mockapp.common.exception.NotFoundException;
import com.example.mockapp.domain.stock.mapper.StockDomainMapper;
import com.example.mockapp.domain.stock.model.Stock;
import com.example.mockapp.persistence.stock.entity.StockEntity;
import com.example.mockapp.persistence.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockDomainMapper mapper;

    public Stock create(Stock stock) {
        StockEntity saved = stockRepository.save(mapper.toEntity(stock));
        return mapper.toDomain(saved);
    }

    public Stock getById(Long warehouseId, Long itemId) {
        StockEntity entity = stockRepository.findByWarehouseIdAndItemId(warehouseId, itemId)
                .orElseThrow(() -> new NotFoundException("Stock not found for warehouseId=" + warehouseId + ", itemId=" + itemId));
        return mapper.toDomain(entity);
    }

    public Stock getByWarehouseAndItem(Long warehouseId, Long itemId) {
        return getById(warehouseId, itemId);
    }

    public Stock getByWarehouseAndItemForUpdate(Long warehouseId, Long itemId) {
        StockEntity entity = stockRepository.findByWarehouseIdAndItemIdForUpdate(warehouseId, itemId)
                .orElseThrow(() -> new NotFoundException("Stock not found for warehouseId=" + warehouseId + ", itemId=" + itemId));
        return mapper.toDomain(entity);
    }

    public Stock update(Long warehouseId, Long itemId, Stock updated) {
        StockEntity existing = stockRepository.findByWarehouseIdAndItemIdForUpdate(warehouseId, itemId)
                .orElseThrow(() -> new NotFoundException("Stock not found for warehouseId=" + warehouseId + ", itemId=" + itemId));

        existing.setWarehouseId(updated.getWarehouseId());
        existing.setItemId(updated.getItemId());
        existing.setQuantity(updated.getQuantity());
        existing.setYearToDate(updated.getYearToDate());
        existing.setOrderCount(updated.getOrderCount());
        existing.setRemoteCount(updated.getRemoteCount());
        existing.setData(updated.getData());

        StockEntity saved = stockRepository.save(existing);
        return mapper.toDomain(saved);
    }
}
