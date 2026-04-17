package com.example.mockapp.persistence.warehouse.entity;

import com.example.mockapp.persistence.district.entity.DistrictEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "WAREHOUSE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "W_ID")
    private Long id;

    @Column(name = "W_NAME", nullable = false, length = 10)
    private String name;

    @Column(name = "W_STREET_1", nullable = false, length = 20)
    private String street1;

    @Column(name = "W_STREET_2", nullable = false, length = 20)
    private String street2;

    @Column(name = "W_CITY", nullable = false, length = 20)
    private String city;

    @Column(name = "W_STATE", nullable = false, length = 2)
    private String state;

    @Column(name = "W_ZIP", nullable = false, length = 9)
    private String zip;

    @Column(name = "W_TAX", precision = 4, scale = 4, nullable = false)
    private BigDecimal tax;

    @Column(name = "W_YTD", precision = 12, scale = 2, nullable = false)
    private BigDecimal yearToDate;

    @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DistrictEntity> districts;
}
