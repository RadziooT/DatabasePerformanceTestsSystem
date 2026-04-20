package com.example.mockapp.persistence.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ORDERS", indexes = {
        @Index(name = "IDX_O_W_ID_O_D_ID_O_C_ID_O_ID", columnList = "O_W_ID,O_D_ID,O_C_ID,O_ID"),
        @Index(name = "IDX_O_W_ID_O_D_ID_O_ID", columnList = "O_W_ID,O_D_ID,O_ID")
})
@IdClass(OrdersEntityId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersEntity {

    @Id
    @Column(name = "O_W_ID", nullable = false)
    private Long warehouseId;

    @Id
    @Column(name = "O_D_ID", nullable = false)
    private Long districtId;

    @Id
    @Column(name = "O_ID", nullable = false)
    private Long id;

    @Column(name = "O_C_ID", nullable = false)
    private Long customerId;

    @Column(name = "O_ENTRY_D", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "O_CARRIER_ID")
    private Long carrierId;

    @Column(name = "O_OL_CNT", nullable = false)
    private Integer orderLineCount;

    @Column(name = "O_ALL_LOCAL", nullable = false)
    private Integer allLocal;
}
