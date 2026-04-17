package example.groups;

import example.endpoints.MockAppEndpoints;
import example.utils.Config;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Session;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;

public class ScenarioGroups {

    private static final String ALL_DISTRICTS_JSON = "[1,2,3,4,5,6,7,8,9,10]";
    private static final String SINGLE_DISTRICT_JSON = "[1]";

    private static int randomInclusive(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static Session prepareWarehouseDistrictCustomer(Session session) {
        return session
                .set("warehouseId", randomInclusive(1, Config.warehouses))
                .set("districtId", randomInclusive(1, Config.districtsPerWarehouse))
                .set("customerId", randomInclusive(1, Config.customersPerDistrict));
    }

    private static Session prepareNewOrder(Session session) {
        int warehouseId = session.getInt("warehouseId");
        boolean allLocal = Config.warehouses == 1 || ThreadLocalRandom.current().nextDouble() < 0.9;

        int supplyWarehouseId2 = warehouseId;
        if (!allLocal && Config.warehouses > 1) {
            do {
                supplyWarehouseId2 = randomInclusive(1, Config.warehouses);
            } while (supplyWarehouseId2 == warehouseId);
        }

        return session
                .set("allLocal", allLocal)
                .set("itemId1", randomInclusive(1, Config.items))
                .set("quantity1", randomInclusive(1, 10))
                .set("itemId2", randomInclusive(1, Config.items))
                .set("supplyWarehouseId2", supplyWarehouseId2)
                .set("quantity2", randomInclusive(1, 10));
    }

    private static Session preparePayment(Session session) {
        double amount = ThreadLocalRandom.current().nextDouble(1.0, 5000.01);
        String paymentAmount = String.format(Locale.US, "%.2f", amount);

        return session
                .set("paymentAmount", paymentAmount)
                .set("paymentData", "counter payment");
    }

    private static Session prepareDelivery(Session session) {
        return session
                .set("carrierId", randomInclusive(1, 10))
                .set("districtIdsJson", ALL_DISTRICTS_JSON);
    }

    private static Session prepareStockLevel(Session session) {
        return session
                .set("stockThreshold", randomInclusive(10, 20))
                .set("recentOrderCount", randomInclusive(10, 30));
    }

    private static Session prepareTpccSmokeTest(Session session) {
        return session
                .set("warehouseId", 1)
                .set("districtId", 1)
                .set("customerId", 1)
                .set("allLocal", true)
                .set("itemId1", 1)
                .set("quantity1", 1)
                .set("itemId2", 1)
                .set("supplyWarehouseId2", 1)
                .set("quantity2", 1)
                .set("paymentAmount", "1.00")
                .set("paymentData", "smoke test")
                .set("carrierId", 1)
                .set("districtIdsJson", SINGLE_DISTRICT_JSON)
                .set("stockThreshold", 1)
                .set("recentOrderCount", 1);
    }

    public static final ChainBuilder newOrder =
            group("NewOrder")
                    .on(
                            exec(ScenarioGroups::prepareWarehouseDistrictCustomer),
                            exec(ScenarioGroups::prepareNewOrder),
                            MockAppEndpoints.newOrder
                    );

    public static final ChainBuilder payment =
            group("Payment")
                    .on(
                            exec(ScenarioGroups::prepareWarehouseDistrictCustomer),
                            exec(ScenarioGroups::preparePayment),
                            MockAppEndpoints.payment
                    );

    public static final ChainBuilder orderStatus =
            group("OrderStatus")
                    .on(
                            exec(ScenarioGroups::prepareWarehouseDistrictCustomer),
                            MockAppEndpoints.orderStatus
                    );

    public static final ChainBuilder delivery =
            group("Delivery")
                    .on(
                            exec(ScenarioGroups::prepareWarehouseDistrictCustomer),
                            exec(ScenarioGroups::prepareDelivery),
                            MockAppEndpoints.delivery
                    );

    public static final ChainBuilder stockLevel =
            group("StockLevel")
                    .on(
                            exec(ScenarioGroups::prepareWarehouseDistrictCustomer),
                            exec(ScenarioGroups::prepareStockLevel),
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
                            exec(ScenarioGroups::prepareTpccSmokeTest),
                            MockAppEndpoints.newOrder,
                            MockAppEndpoints.payment,
                            MockAppEndpoints.orderStatus,
                            MockAppEndpoints.delivery,
                            MockAppEndpoints.stockLevel
                    );
}
