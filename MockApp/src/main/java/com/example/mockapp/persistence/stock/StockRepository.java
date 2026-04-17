package com.example.mockapp.persistence.stock;

import com.example.mockapp.persistence.stock.entity.StockEntity;
import com.example.mockapp.persistence.stock.entity.StockEntityId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<StockEntity, StockEntityId> {

	Optional<StockEntity> findByWarehouseIdAndItemId(Long warehouseId, Long itemId);

	List<StockEntity> findByWarehouseIdOrderByItemId(Long warehouseId);
}
