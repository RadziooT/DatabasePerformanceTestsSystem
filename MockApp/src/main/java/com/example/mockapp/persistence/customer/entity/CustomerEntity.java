package com.example.mockapp.persistence.customer.entity;

import com.example.mockapp.persistence.district.entity.DistrictEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "CUSTOMER", indexes = {
    @Index(name = "IDX_C_W_ID_C_D_ID", columnList = "C_W_ID,C_D_ID"),
    @Index(name = "IDX_C_W_ID_C_D_ID_C_LAST", columnList = "C_W_ID,C_D_ID,C_LAST,C_FIRST")
})
@IdClass(CustomerEntityId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerEntity {

    @Id
    @Column(name = "C_W_ID", nullable = false)
    @EqualsAndHashCode.Include
    private Long warehouseId;

    @Id
    @Column(name = "C_D_ID", nullable = false)
    @EqualsAndHashCode.Include
    private Long districtId;

    @Id
    @Column(name = "C_ID", nullable = false)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "C_W_ID", referencedColumnName = "D_W_ID", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "C_D_ID", referencedColumnName = "D_ID", nullable = false, insertable = false, updatable = false)
    })
    @ToString.Exclude
    private DistrictEntity district;

    @Column(name = "C_FIRST", nullable = false, length = 16)
    private String firstName;

    @Column(name = "C_MIDDLE", nullable = false, length = 2)
    private String middleName;

    @Column(name = "C_LAST", nullable = false, length = 16)
    private String lastName;

    @Column(name = "C_STREET_1", nullable = false, length = 20)
    private String street1;

    @Column(name = "C_STREET_2", nullable = false, length = 20)
    private String street2;

    @Column(name = "C_CITY", nullable = false, length = 20)
    private String city;

    @Column(name = "C_STATE", nullable = false, length = 2)
    private String state;

    @Column(name = "C_ZIP", nullable = false, length = 9)
    private String zip;

    @Column(name = "C_PHONE", nullable = false, length = 16)
    private String phone;

    @Column(name = "C_SINCE", nullable = false)
    private java.time.LocalDateTime since;

    @Column(name = "C_CREDIT", nullable = false, length = 2)
    private String credit;

    @Column(name = "C_CREDIT_LIM", precision = 12, scale = 2, nullable = false)
    private BigDecimal creditLimit;

    @Column(name = "C_DISCOUNT", precision = 4, scale = 4, nullable = false)
    private BigDecimal discount;

    @Column(name = "C_BALANCE", precision = 12, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(name = "C_YTD_PAYMENT", precision = 12, scale = 2, nullable = false)
    private BigDecimal yearToDatePayment;

    @Column(name = "C_PAYMENT_CNT", nullable = false)
    private Integer paymentCount;

    @Column(name = "C_DELIVERY_CNT", nullable = false)
    private Integer deliveryCount;

    @Column(name = "C_DATA", nullable = false, length = 500)
    private String data;
}
