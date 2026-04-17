package com.example.mockapp.api;

import com.example.mockapp.api.model.DeliveryRequest;
import com.example.mockapp.api.model.DeliveryResponse;
import com.example.mockapp.api.model.NewOrderRequest;
import com.example.mockapp.api.model.NewOrderResponse;
import com.example.mockapp.api.model.OrderStatusRequest;
import com.example.mockapp.api.model.OrderStatusResponse;
import com.example.mockapp.api.model.PaymentRequest;
import com.example.mockapp.api.model.PaymentResponse;
import com.example.mockapp.api.model.StockLevelRequest;
import com.example.mockapp.api.model.StockLevelResponse;
import com.example.mockapp.domain.TpccTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TpcController {

    private final TpccTransactionService tpccTransactionService;

    @PostMapping("/transactions/new-order")
    public ResponseEntity<NewOrderResponse> newOrder(@RequestBody NewOrderRequest request) {
        log.info("Executing TPC-C New-Order transaction for customerId={}", request.getCustomerId());
        return ResponseEntity.ok(tpccTransactionService.createNewOrder(request));
    }

    @PostMapping("/transactions/payment")
    public ResponseEntity<PaymentResponse> payment(@RequestBody PaymentRequest request) {
        log.info("Executing TPC-C Payment transaction for warehouseId={}, districtId={}",
                request.getWarehouseId(), request.getDistrictId());
        return ResponseEntity.ok(tpccTransactionService.payment(request));
    }

    @PostMapping("/transactions/order-status")
    public ResponseEntity<OrderStatusResponse> orderStatus(@RequestBody OrderStatusRequest request) {
        log.info("Executing TPC-C Order-Status transaction for districtId={}", request.getDistrictId());
        return ResponseEntity.ok(tpccTransactionService.orderStatus(request));
    }

    @PostMapping("/transactions/delivery")
    public ResponseEntity<DeliveryResponse> delivery(@RequestBody DeliveryRequest request) {
        log.info("Executing TPC-C Delivery transaction for warehouseId={}, carrierId={}",
                request.getWarehouseId(), request.getCarrierId());
        return ResponseEntity.ok(tpccTransactionService.delivery(request));
    }

    @PostMapping("/transactions/stock-level")
    public ResponseEntity<StockLevelResponse> stockLevel(@RequestBody StockLevelRequest request) {
        log.info("Executing TPC-C Stock-Level transaction for warehouseId={}, districtId={}",
                request.getWarehouseId(), request.getDistrictId());
        return ResponseEntity.ok(tpccTransactionService.stockLevel(request));
    }
}
