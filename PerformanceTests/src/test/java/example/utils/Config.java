package example.utils;

import io.gatling.javaapi.http.HttpProtocolBuilder;
import lombok.experimental.UtilityClass;

import static io.gatling.javaapi.http.HttpDsl.http;

@UtilityClass
public class Config {

    public static String baseUrl = System.getProperty(
            "baseUrl",
            System.getenv().getOrDefault("BASE_URL", "http://localhost:8080"));

    public static HttpProtocolBuilder httpProtocol() {
        return http.baseUrl(baseUrl);
    }

    public static final DatasetSize datasetSize = DatasetSize.parse(
            System.getenv().getOrDefault("DATASET_SIZE", "small"));

    public static final int warehouses = datasetSize.warehouses();

    public static final int districtsPerWarehouse = TpcConstants.districtsPerWarehouse;
    public static final int customersPerDistrict = TpcConstants.customersPerDistrict;
    public static final int items = TpcConstants.items;

    public static int userCountFor(WorkloadType workloadType) {
        return WorkloadUserCounts.userCountFor(datasetSize, workloadType);
    }
}
