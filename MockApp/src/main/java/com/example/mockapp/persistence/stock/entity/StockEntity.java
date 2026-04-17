package com.example.mockapp.persistence.stock.entity;

import com.example.mockapp.persistence.item.entity.ItemEntity;
import com.example.mockapp.persistence.warehouse.entity.WarehouseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STOCK", indexes = {
        @Index(name = "IDX_S_I_ID", columnList = "S_I_ID")
})
@IdClass(StockEntityId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockEntity {

    @Id
    @Column(name = "S_W_ID", nullable = false)
    private Long warehouseId;

    @Id
    @Column(name = "S_I_ID", nullable = false)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "S_W_ID", referencedColumnName = "W_ID", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK_STOCK_WAREHOUSE"))
    private WarehouseEntity warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "S_I_ID", referencedColumnName = "I_ID", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK_STOCK_ITEM"))
    private ItemEntity item;

    @Column(name = "S_QUANTITY", nullable = false)
    private Integer quantity;

    @Column(name = "S_DIST_01", nullable = false, length = 24)
    private String district01;

    @Column(name = "S_DIST_02", nullable = false, length = 24)
    private String district02;

    @Column(name = "S_DIST_03", nullable = false, length = 24)
    private String district03;

    @Column(name = "S_DIST_04", nullable = false, length = 24)
    private String district04;

    @Column(name = "S_DIST_05", nullable = false, length = 24)
    private String district05;

    @Column(name = "S_DIST_06", nullable = false, length = 24)
    private String district06;

    @Column(name = "S_DIST_07", nullable = false, length = 24)
    private String district07;

    @Column(name = "S_DIST_08", nullable = false, length = 24)
    private String district08;

    @Column(name = "S_DIST_09", nullable = false, length = 24)
    private String district09;

    @Column(name = "S_DIST_10", nullable = false, length = 24)
    private String district10;

    @Column(name = "S_YTD", nullable = false)
    private Integer yearToDate;

    @Column(name = "S_ORDER_CNT", nullable = false)
    private Integer orderCount;

    @Column(name = "S_REMOTE_CNT", nullable = false)
    private Integer remoteCount;

    @Column(name = "S_DATA", nullable = false, length = 50)
    private String data;
}
