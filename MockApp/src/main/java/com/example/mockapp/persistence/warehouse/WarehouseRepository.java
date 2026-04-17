package com.example.mockapp.persistence.warehouse;

import com.example.mockapp.persistence.warehouse.entity.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<WarehouseEntity, Long> {
}
