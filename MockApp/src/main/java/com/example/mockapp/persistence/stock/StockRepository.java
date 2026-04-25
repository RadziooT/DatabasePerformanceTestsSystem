package com.example.mockapp.persistence.stock;

import com.example.mockapp.persistence.stock.entity.StockEntity;
import com.example.mockapp.persistence.stock.entity.StockEntityId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockRepository extends JpaRepository<StockEntity, StockEntityId> {

    Optional<StockEntity> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockEntity s where s.warehouseId = :warehouseId and s.itemId = :itemId")
    Optional<StockEntity> findByWarehouseIdAndItemIdForUpdate(@Param("warehouseId") Long warehouseId,
                                                               @Param("itemId") Long itemId);
}
