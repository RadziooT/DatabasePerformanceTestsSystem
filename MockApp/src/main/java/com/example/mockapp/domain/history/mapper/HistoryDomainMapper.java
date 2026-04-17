package com.example.mockapp.domain.history.mapper;

import com.example.mockapp.domain.DomainPersistenceMapper;
import com.example.mockapp.domain.history.model.History;
import com.example.mockapp.persistence.history.entity.HistoryEntity;
import org.springframework.stereotype.Component;

@Component
public class HistoryDomainMapper implements DomainPersistenceMapper<History, HistoryEntity> {

	@Override
	public HistoryEntity toEntity(History history) {
		if (history == null) {
			return null;
		}
		return HistoryEntity.builder()
				.id(history.getId())
				.customerId(history.getCustomerId())
				.customerDistrictId(history.getCustomerDistrictId())
				.customerWarehouseId(history.getCustomerWarehouseId())
				.districtId(history.getDistrictId())
				.warehouseId(history.getWarehouseId())
				.date(history.getDate())
				.amount(history.getAmount())
				.data(history.getData())
				.build();
	}

	@Override
	public History toDomain(HistoryEntity entity) {
		if (entity == null) {
			return null;
		}
		return History.builder()
				.id(entity.getId())
				.customerId(entity.getCustomerId())
				.customerDistrictId(entity.getCustomerDistrictId())
				.customerWarehouseId(entity.getCustomerWarehouseId())
				.districtId(entity.getDistrictId())
				.warehouseId(entity.getWarehouseId())
				.date(entity.getDate())
				.amount(entity.getAmount())
				.data(entity.getData())
				.build();
	}
}
