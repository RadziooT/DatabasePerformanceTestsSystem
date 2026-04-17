package com.example.mockapp.persistence.neworder.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "NEW_ORDER")
@IdClass(NewOrderEntityId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewOrderEntity {

    @Id
    @Column(name = "NO_W_ID", nullable = false)
    private Long warehouseId;

    @Id
    @Column(name = "NO_D_ID", nullable = false)
    private Long districtId;

    @Id
    @Column(name = "NO_O_ID", nullable = false)
    private Long orderId;
}
