package com.example.mockapp.persistence.district.entity;

import com.example.mockapp.persistence.customer.entity.CustomerEntity;
import com.example.mockapp.persistence.warehouse.entity.WarehouseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "DISTRICT", indexes = {
    @Index(name = "IDX_D_W_ID", columnList = "D_W_ID"),
    @Index(name = "IDX_D_W_ID_D_ID", columnList = "D_W_ID,D_ID")
})
@IdClass(DistrictEntityId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DistrictEntity {

    @Id
    @Column(name = "D_W_ID", nullable = false)
    @EqualsAndHashCode.Include
    private Long warehouseId;

    @Id
    @Column(name = "D_ID")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "D_W_ID", referencedColumnName = "W_ID", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK_DISTRICT_WAREHOUSE"))
    @ToString.Exclude
    private WarehouseEntity warehouse;

    @Column(name = "D_NAME", nullable = false, length = 10)
    private String name;

    @Column(name = "D_STREET_1", nullable = false, length = 20)
    private String street1;

    @Column(name = "D_STREET_2", nullable = false, length = 20)
    private String street2;

    @Column(name = "D_CITY", nullable = false, length = 20)
    private String city;

    @Column(name = "D_STATE", nullable = false, length = 2)
    private String state;

    @Column(name = "D_ZIP", nullable = false, length = 9)
    private String zip;

    @Column(name = "D_TAX", precision = 4, scale = 4, nullable = false)
    private BigDecimal tax;

    @Column(name = "D_YTD", precision = 12, scale = 2, nullable = false)
    private BigDecimal yearToDate;

    @Column(name = "D_NEXT_O_ID", nullable = false)
    private Long nextOrderId;

    @OneToMany(mappedBy = "district", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<CustomerEntity> customers;
}
