package com.example.mockapp.persistence.item.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ITEM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEntity {

    @Id
    @Column(name = "I_ID", nullable = false)
    private Long id;

    @Column(name = "I_IM_ID")
    private Long imageId;

    @Column(name = "I_NAME", nullable = false, length = 24)
    private String name;

    @Column(name = "I_PRICE", precision = 5, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "I_DATA", nullable = false, length = 50)
    private String data;
}
