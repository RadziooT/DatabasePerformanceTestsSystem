package com.example.mockapp.persistence.history.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "HISTORY", indexes = {
        @Index(name = "IDX_H_C_W_C_D_C_ID", columnList = "H_C_W_ID,H_C_D_ID,H_C_ID"),
        @Index(name = "IDX_H_W_D", columnList = "H_W_ID,H_D_ID")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "H_ID")
    private Long id;

    @Column(name = "H_C_ID", nullable = false)
    private Long customerId;

    @Column(name = "H_C_D_ID", nullable = false)
    private Long customerDistrictId;

    @Column(name = "H_C_W_ID", nullable = false)
    private Long customerWarehouseId;

    @Column(name = "H_D_ID", nullable = false)
    private Long districtId;

    @Column(name = "H_W_ID", nullable = false)
    private Long warehouseId;

    @Column(name = "H_DATE", nullable = false)
    private LocalDateTime date;

    @Column(name = "H_AMOUNT", precision = 6, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "H_DATA", nullable = false, length = 24)
    private String data;
}
