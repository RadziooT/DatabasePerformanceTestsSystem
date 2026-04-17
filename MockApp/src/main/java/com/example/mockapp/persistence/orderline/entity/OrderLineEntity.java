package com.example.mockapp.persistence.orderline.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ORDER_LINE")
@IdClass(OrderLineEntityId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineEntity {

    @Id
    @Column(name = "OL_W_ID", nullable = false)
    private Long warehouseId;

    @Id
    @Column(name = "OL_D_ID", nullable = false)
    private Long districtId;

    @Id
    @Column(name = "OL_O_ID", nullable = false)
    private Long orderId;

    @Id
    @Column(name = "OL_NUMBER", nullable = false)
    private Integer lineNumber;

    @Column(name = "OL_I_ID", nullable = false)
    private Long itemId;

    @Column(name = "OL_SUPPLY_W_ID", nullable = false)
    private Long supplyWarehouseId;

    @Column(name = "OL_DELIVERY_D")
    private LocalDateTime deliveryDate;

    @Column(name = "OL_QUANTITY", nullable = false)
    private Integer quantity;

    @Column(name = "OL_AMOUNT", nullable = false, precision = 6, scale = 2)
    private BigDecimal amount;

    @Column(name = "OL_DIST_INFO", nullable = false, length = 24)
    private String distInfo;
}
