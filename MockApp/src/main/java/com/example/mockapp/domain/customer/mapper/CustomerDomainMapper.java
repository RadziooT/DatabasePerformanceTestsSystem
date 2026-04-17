package com.example.mockapp.domain.customer.mapper;

import com.example.mockapp.domain.DomainPersistenceMapper;
import com.example.mockapp.domain.customer.model.Customer;
import com.example.mockapp.persistence.customer.entity.CustomerEntity;
import com.example.mockapp.persistence.district.entity.DistrictEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerDomainMapper implements DomainPersistenceMapper<Customer, CustomerEntity> {

    public CustomerEntity toEntity(Customer customer, DistrictEntity district) {
        if (customer == null && district == null) {
            return null;
        }
        CustomerEntity.CustomerEntityBuilder builder = CustomerEntity.builder();
        if (customer != null) {
            builder
                    .warehouseId(customer.getWarehouseId())
                    .districtId(customer.getDistrictId())
                    .id(customer.getId())
                    .firstName(customer.getFirstName())
                    .lastName(customer.getLastName())
                    .balance(customer.getBalance())
                    .paymentCount(customer.getPaymentCount());
        }
        if (district != null) {
            builder.warehouseId(district.getWarehouseId());
            builder.districtId(district.getId());
            builder.district(district);
        }
        return builder.build();
    }

    @Override
    public CustomerEntity toEntity(Customer customer) {
        return toEntity(customer, null);
    }

    @Override
    public Customer toDomain(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }
        return Customer.builder()
                .id(entity.getId())
                .warehouseId(entity.getWarehouseId())
                .districtId(entity.getDistrictId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .balance(entity.getBalance())
                .paymentCount(entity.getPaymentCount())
                .build();
    }
}
