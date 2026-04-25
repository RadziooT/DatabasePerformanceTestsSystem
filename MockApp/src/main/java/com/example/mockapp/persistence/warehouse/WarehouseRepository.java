package com.example.mockapp.persistence.warehouse;

import com.example.mockapp.persistence.warehouse.entity.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<WarehouseEntity, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select w from WarehouseEntity w where w.id = :id")
	Optional<WarehouseEntity> findByIdForUpdate(@Param("id") Long id);
}
