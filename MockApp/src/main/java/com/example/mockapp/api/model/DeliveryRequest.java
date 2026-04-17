package com.example.mockapp.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRequest {

    private Long warehouseId;
    private Long carrierId;
    private LocalDateTime deliveryDate;
    private List<Long> districtIds;
}
