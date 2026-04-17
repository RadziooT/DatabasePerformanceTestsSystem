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
public class DeliveryResponse {

    private Long warehouseId;
    private Long carrierId;
    private LocalDateTime deliveryDate;
    private List<DeliveredOrderResponse> deliveredOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveredOrderResponse {
        private Long districtId;
        private Long orderId;
        private Long customerId;
        private Integer lineCount;
    }
}
