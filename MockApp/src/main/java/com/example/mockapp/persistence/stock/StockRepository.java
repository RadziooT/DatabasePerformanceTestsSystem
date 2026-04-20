package com.example.mockapp.persistence.stock;

import com.example.mockapp.persistence.stock.entity.StockEntity;
import com.example.mockapp.persistence.stock.entity.StockEntityId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface StockRepository extends JpaRepository<StockEntity, StockEntityId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StockEntity> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);
}
