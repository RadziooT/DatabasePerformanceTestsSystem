package example.groups;

import example.utils.Config;
import example.utils.TpcConstants;
import io.gatling.javaapi.core.Session;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class PayloadDataGenerator {


    private static final int NEW_ORDER_MIN_LINES = 5;
    private static final int NEW_ORDER_MAX_LINES = 15;
    private static final int PAYMENT_REMOTE_CUSTOMER_PERCENT = 15;
    private static final int STOCK_LEVEL_MIN_THRESHOLD = 10;
    private static final int STOCK_LEVEL_MAX_THRESHOLD = 20;
    private static final int STOCK_LEVEL_RECENT_ORDER_COUNT = 20;

    public Session prepareNewOrder(Session session) {
        return prepareNewOrder(session, false);
    }

    public Session prepareNewOrderRollback(Session session) {
        return prepareNewOrder(session, true);
    }

    public Session preparePaymentByLastName(Session session) {
        return preparePayment(session, true);
    }

    public Session preparePaymentById(Session session) {
        return preparePayment(session, false);
    }

    public Session prepareOrderStatusByLastName(Session session) {
        return prepareOrderStatus(session, true);
    }

    public Session prepareOrderStatusById(Session session) {
        return prepareOrderStatus(session, false);
    }

    public Session prepareDelivery(Session session) {
        return session.set("deliveryPayload", PayloadJsonBuilder.buildDeliveryPayload(randomWarehouseId(), randomInclusive(1, 10)));
    }

    public Session prepareStockLevel(Session session) {
        int warehouseId = randomWarehouseId();
        int districtId = randomDistrictId();
        int threshold = randomInclusive(STOCK_LEVEL_MIN_THRESHOLD, STOCK_LEVEL_MAX_THRESHOLD);

        return session.set("stockLevelPayload", PayloadJsonBuilder.buildStockLevelPayload(warehouseId, districtId, threshold, STOCK_LEVEL_RECENT_ORDER_COUNT));
    }

    public Session prepareTpcSmokeTest(Session session) {
        List<LineItemPayload> items = List.of(
                new LineItemPayload(1, 1, 1),
                new LineItemPayload(2, 1, 1),
                new LineItemPayload(3, 1, 1),
                new LineItemPayload(4, 1, 1),
                new LineItemPayload(5, 1, 1)
        );

        return session
                .set("newOrderPayload", PayloadJsonBuilder.buildNewOrderPayload(1, 1, 1, true, items))
                .set("paymentPayload", PayloadJsonBuilder.buildPaymentPayload(1, 1, 1, 1, 1, null, "1.00"))
                .set("orderStatusPayload", PayloadJsonBuilder.buildOrderStatusPayload(1, 1, 1, 1, 1, null))
                .set("deliveryPayload", PayloadJsonBuilder.buildDeliveryPayload(1, 1))
                .set("stockLevelPayload", PayloadJsonBuilder.buildStockLevelPayload(1, 1, 10, STOCK_LEVEL_RECENT_ORDER_COUNT));
    }

    private Session prepareNewOrder(Session session, boolean forceInvalidItem) {
        int warehouseId = randomWarehouseId();
        int districtId = randomDistrictId();
        int customerId = randomCustomerId();
        int itemCount = randomInclusive(NEW_ORDER_MIN_LINES, NEW_ORDER_MAX_LINES);
        boolean allLocal = Config.warehouses == 1 || ThreadLocalRandom.current().nextDouble() < 0.9;
        int remoteLine = !allLocal && Config.warehouses > 1 ? randomInclusive(1, itemCount) : -1;
        int invalidLine = forceInvalidItem ? randomInclusive(1, itemCount) : -1;

        List<LineItemPayload> items = new ArrayList<>(itemCount);
        for (int line = 1; line <= itemCount; line++) {
            int itemId = line == invalidLine
                    ? Config.items + randomInclusive(1, 1000)
                    : randomInclusive(1, Config.items);
            int supplyWarehouseId = line == remoteLine ? randomRemoteWarehouseId(warehouseId) : warehouseId;
            int quantity = randomInclusive(1, 10);
            items.add(new LineItemPayload(itemId, quantity, supplyWarehouseId));
        }

        return session.set("newOrderPayload", PayloadJsonBuilder.buildNewOrderPayload(warehouseId, districtId, customerId, allLocal, items));
    }

    private Session preparePayment(Session session, boolean lookupByLastName) {
        int warehouseId = randomWarehouseId();
        int districtId = randomDistrictId();
        boolean remoteCustomer = Config.warehouses > 1 && ThreadLocalRandom.current().nextInt(100) < PAYMENT_REMOTE_CUSTOMER_PERCENT;
        int customerWarehouseId = remoteCustomer ? randomRemoteWarehouseId(warehouseId) : warehouseId;
        int customerDistrictId = randomDistrictId();
        int customerId = randomCustomerId();
        String customerLastName = customerLastNameFor(customerId);
        String paymentAmount = String.format(Locale.US, "%.2f", ThreadLocalRandom.current().nextDouble(1.0, 5000.01));

        return session.set("paymentPayload", PayloadJsonBuilder.buildPaymentPayload(
                warehouseId,
                districtId,
                customerWarehouseId,
                customerDistrictId,
                lookupByLastName ? null : customerId,
                lookupByLastName ? customerLastName : null,
                paymentAmount));
    }

    private Session prepareOrderStatus(Session session, boolean lookupByLastName) {
        int warehouseId = randomWarehouseId();
        int districtId = randomDistrictId();
        int customerId = randomCustomerId();
        String customerLastName = customerLastNameFor(customerId);

        return session.set("orderStatusPayload", PayloadJsonBuilder.buildOrderStatusPayload(
                warehouseId,
                districtId,
                warehouseId,
                districtId,
                lookupByLastName ? null : customerId,
                lookupByLastName ? customerLastName : null));
    }

    private int randomInclusive(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private int randomWarehouseId() {
        return randomInclusive(1, Config.warehouses);
    }

    private int randomDistrictId() {
        return randomInclusive(1, Config.districtsPerWarehouse);
    }

    private int randomCustomerId() {
        return randomInclusive(1, Config.customersPerDistrict);
    }

    private int randomRemoteWarehouseId(int homeWarehouseId) {
        if (Config.warehouses <= 1) {
            return homeWarehouseId;
        }

        int remoteWarehouseId;
        do {
            remoteWarehouseId = randomWarehouseId();
        } while (remoteWarehouseId == homeWarehouseId);
        return remoteWarehouseId;
    }

    private String customerLastNameFor(int customerId) {
        return TpcConstants.customerLastNameFor(customerId);
    }
}
