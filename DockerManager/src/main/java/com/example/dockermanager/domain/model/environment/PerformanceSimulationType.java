package com.example.dockermanager.domain.model.environment;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum PerformanceSimulationType {
    BASIC_SIMULATION("example.BasicSimulation"),
    MixedTpccSimulation("example.MixedTpccSimulation"),
    NewOrderSimulation("example.NewOrderSimulation"),
    PaymentSimulation("example.PaymentSimulation"),
    OrderStatusSimulation("example.OrderStatusSimulation"),
    DeliverySimulation("example.DeliverySimulation"),
    StockLevelSimulation("example.StockLevelSimulation");

    private final String simulationClass;

    PerformanceSimulationType(String simulationClass) {
        this.simulationClass = simulationClass;
    }

    public static List<String> getNames() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}
