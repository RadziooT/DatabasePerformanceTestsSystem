package example.groups;

import example.endpoints.MockAppEndpoints;
import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;

public class ScenarioGroups {

    private static final int PAYMENT_LAST_NAME_PERCENT = 60;

    public static final ChainBuilder delivery =
            group("Delivery")
                    .on(
                            exec(PayloadDataGenerator::prepareDelivery),
                            MockAppEndpoints.delivery
                    );

    public static final ChainBuilder newOrder =
            group("NewOrder")
                    .on(
                            randomSwitch().on(
                                    percent(99.0).then(exec(PayloadDataGenerator::prepareNewOrder), MockAppEndpoints.newOrder),
                                    percent(1.0).then(exec(PayloadDataGenerator::prepareNewOrderRollback), MockAppEndpoints.newOrderRollback)
                            )
                    );

    public static final ChainBuilder orderStatus =
            group("OrderStatus")
                    .on(
                            randomSwitch().on(
                                    percent(PAYMENT_LAST_NAME_PERCENT)
                                            .then(exec(PayloadDataGenerator::prepareOrderStatusByLastName), MockAppEndpoints.orderStatus),
                                    percent(100 - PAYMENT_LAST_NAME_PERCENT)
                                            .then(exec(PayloadDataGenerator::prepareOrderStatusById), MockAppEndpoints.orderStatus)
                            )
                    );

    public static final ChainBuilder payment =
            group("Payment")
                    .on(
                            randomSwitch().on(
                                    percent(PAYMENT_LAST_NAME_PERCENT)
                                            .then(exec(PayloadDataGenerator::preparePaymentByLastName), MockAppEndpoints.payment),
                                    percent(100 - PAYMENT_LAST_NAME_PERCENT)
                                            .then(exec(PayloadDataGenerator::preparePaymentById), MockAppEndpoints.payment)
                            )
                    );

    public static final ChainBuilder stockLevel =
            group("StockLevel")
                    .on(
                            exec(PayloadDataGenerator::prepareStockLevel),
                            MockAppEndpoints.stockLevel
                    );

    public static final ChainBuilder mixedWorkload =
            group("MixedWorkload")
                    .on(
                            randomSwitch().on(
                                    percent(45.0).then(newOrder),
                                    percent(43.0).then(payment),
                                    percent(4.0).then(orderStatus),
                                    percent(4.0).then(delivery),
                                    percent(4.0).then(stockLevel)
                            )
                    );

    public static final ChainBuilder basicSmokeTest =
            group("SmokeTest")
                    .on(
                            exec(PayloadDataGenerator::prepareTpcSmokeTest),
                            MockAppEndpoints.newOrder,
                            MockAppEndpoints.payment,
                            MockAppEndpoints.orderStatus,
                            MockAppEndpoints.delivery,
                            MockAppEndpoints.stockLevel
                    );
}
