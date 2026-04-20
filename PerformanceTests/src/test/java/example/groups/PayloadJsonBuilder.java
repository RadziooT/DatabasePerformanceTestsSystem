package example.groups;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Locale;

@UtilityClass
public class PayloadJsonBuilder {

    private static final String ALL_DISTRICTS_JSON = "[1,2,3,4,5,6,7,8,9,10]";

    public static String buildNewOrderPayload(int warehouseId, int districtId, int customerId, boolean allLocal, List<LineItemPayload> items) {
        StringBuilder payload = new StringBuilder();
        payload.append("{\"warehouseId\":").append(warehouseId)
                .append(",\"districtId\":").append(districtId)
                .append(",\"customerId\":").append(customerId)
                .append(",\"allLocal\":").append(allLocal)
                .append(",\"items\":[");

        for (int i = 0; i < items.size(); i++) {
            LineItemPayload item = items.get(i);
            if (i > 0) {
                payload.append(',');
            }
            payload.append("{\"itemId\":").append(item.itemId())
                    .append(",\"quantity\":").append(item.quantity());
            if (item.supplyWarehouseId() != warehouseId) {
                payload.append(",\"supplyWarehouseId\":").append(item.supplyWarehouseId());
            }
            payload.append('}');
        }

        return payload.append("]}").toString();
    }

    public static String buildPaymentPayload(
            int warehouseId,
            int districtId,
            int customerWarehouseId,
            int customerDistrictId,
            Integer customerId,
            String customerLastName,
            String paymentAmount
    ) {
        return customerId != null
                ? String.format(Locale.US,
                "{\"warehouseId\":%d,\"districtId\":%d,\"customerWarehouseId\":%d,\"customerDistrictId\":%d,\"customerId\":%d,\"amount\":%s,\"data\":\"counter payment\"}",
                warehouseId, districtId, customerWarehouseId, customerDistrictId, customerId, paymentAmount)
                : String.format(Locale.US,
                "{\"warehouseId\":%d,\"districtId\":%d,\"customerWarehouseId\":%d,\"customerDistrictId\":%d,\"customerLastName\":\"%s\",\"amount\":%s,\"data\":\"counter payment\"}",
                warehouseId, districtId, customerWarehouseId, customerDistrictId, customerLastName, paymentAmount);
    }

    public static String buildOrderStatusPayload(
            int warehouseId,
            int districtId,
            int customerWarehouseId,
            int customerDistrictId,
            Integer customerId,
            String customerLastName
    ) {
        return customerId != null
                ? String.format(Locale.US,
                "{\"warehouseId\":%d,\"districtId\":%d,\"customerWarehouseId\":%d,\"customerDistrictId\":%d,\"customerId\":%d}",
                warehouseId, districtId, customerWarehouseId, customerDistrictId, customerId)
                : String.format(Locale.US,
                "{\"warehouseId\":%d,\"districtId\":%d,\"customerWarehouseId\":%d,\"customerDistrictId\":%d,\"customerLastName\":\"%s\"}",
                warehouseId, districtId, customerWarehouseId, customerDistrictId, customerLastName);
    }

    public static String buildDeliveryPayload(int warehouseId, int carrierId) {
        return String.format(Locale.US,
                "{\"warehouseId\":%d,\"carrierId\":%d,\"districtIds\":%s}",
                warehouseId, carrierId, ALL_DISTRICTS_JSON);
    }

    public static String buildStockLevelPayload(int warehouseId, int districtId, int threshold, int recentOrderCount) {
        return String.format(Locale.US,
                "{\"warehouseId\":%d,\"districtId\":%d,\"threshold\":%d,\"recentOrderCount\":%d}",
                warehouseId,
                districtId,
                threshold,
                recentOrderCount);
    }
}
