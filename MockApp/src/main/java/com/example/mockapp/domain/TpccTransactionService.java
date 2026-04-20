package com.example.mockapp.domain;

import com.example.mockapp.api.model.*;
import com.example.mockapp.domain.order.service.NewOrderTransaction;
import com.example.mockapp.domain.order.service.OrderStatusTransaction;
import com.example.mockapp.domain.stock.service.StockLevelTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TpccTransactionService {

    private final NewOrderTransaction newOrderTransaction;
    private final PaymentTransaction paymentTransaction;
    private final OrderStatusTransaction orderStatusTransaction;
    private final DeliveryTransaction deliveryTransaction;
    private final StockLevelTransaction stockLevelTransaction;

    public NewOrderResponse createNewOrder(NewOrderRequest request) {
        return newOrderTransaction.execute(request);
    }

    public PaymentResponse payment(PaymentRequest request) {
        return paymentTransaction.execute(request);
    }

    public OrderStatusResponse orderStatus(OrderStatusRequest request) {
        return orderStatusTransaction.execute(request);
    }

    public DeliveryResponse delivery(DeliveryRequest request) {
        return deliveryTransaction.execute(request);
    }

    public StockLevelResponse stockLevel(StockLevelRequest request) {
        return stockLevelTransaction.execute(request);
    }
}
