package example.endpoints;

import io.gatling.javaapi.http.HttpRequestActionBuilder;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class MockAppEndpoints {

    private static final String BASE_PATH = "/api/transactions";

    public static final HttpRequestActionBuilder newOrder = http("TPC-C New-Order")
            .post(BASE_PATH + "/new-order")
            .header("Content-Type", "application/json")
            .body(StringBody("{\"warehouseId\":#{warehouseId},\"districtId\":#{districtId},\"customerId\":#{customerId},\"allLocal\":#{allLocal},\"items\":[{\"itemId\":#{itemId1},\"quantity\":#{quantity1}},{\"itemId\":#{itemId2},\"supplyWarehouseId\":#{supplyWarehouseId2},\"quantity\":#{quantity2}}]}"))
            .check(status().in(200, 201));

    public static final HttpRequestActionBuilder payment = http("TPC-C Payment")
            .post(BASE_PATH + "/payment")
            .header("Content-Type", "application/json")
            .body(StringBody("{\"warehouseId\":#{warehouseId},\"districtId\":#{districtId},\"customerId\":#{customerId},\"amount\":#{paymentAmount},\"data\":\"#{paymentData}\"}"))
            .check(status().in(200, 201));

    public static final HttpRequestActionBuilder orderStatus = http("TPC-C Order-Status")
            .post(BASE_PATH + "/order-status")
            .header("Content-Type", "application/json")
            .body(StringBody("{\"warehouseId\":#{warehouseId},\"districtId\":#{districtId},\"customerId\":#{customerId}}"))
            .check(status().is(200));

    public static final HttpRequestActionBuilder delivery = http("TPC-C Delivery")
            .post(BASE_PATH + "/delivery")
            .header("Content-Type", "application/json")
            .body(StringBody("{\"warehouseId\":#{warehouseId},\"carrierId\":#{carrierId},\"districtIds\":#{districtIdsJson}}"))
            .check(status().in(200, 201));

    public static final HttpRequestActionBuilder stockLevel = http("TPC-C Stock-Level")
            .post(BASE_PATH + "/stock-level")
            .header("Content-Type", "application/json")
            .body(StringBody("{\"warehouseId\":#{warehouseId},\"districtId\":#{districtId},\"threshold\":#{stockThreshold},\"recentOrderCount\":#{recentOrderCount}}"))
            .check(status().is(200));
}
