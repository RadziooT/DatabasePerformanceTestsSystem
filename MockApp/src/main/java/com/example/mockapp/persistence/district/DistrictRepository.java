package com.example.mockapp.persistence.district;

import com.example.mockapp.persistence.district.entity.DistrictEntity;
import com.example.mockapp.persistence.district.entity.DistrictEntityId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DistrictRepository extends JpaRepository<DistrictEntity, DistrictEntityId> {

    Optional<DistrictEntity> findByWarehouseIdAndId(Long warehouseId, Long districtId);

    Optional<DistrictEntity> findTopByWarehouseIdOrderByIdDesc(Long warehouseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DistrictEntity d where d.warehouseId = :warehouseId and d.id = :districtId")
    Optional<DistrictEntity> findByWarehouseIdAndIdForUpdate(@Param("warehouseId") Long warehouseId,
                                                             @Param("districtId") Long districtId);
}
