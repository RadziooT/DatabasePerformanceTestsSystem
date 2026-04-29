package example.utils;

import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;

@UtilityClass
public final class SimulationConfiguration {

    public static PopulationBuilder injectionProfile(ScenarioBuilder scenario) {
        return switch (Config.datasetSize) {
            case SMALL -> scenario.injectClosed(
                    rampConcurrentUsers(0).to(10).during(Duration.ofSeconds(30)),
                    constantConcurrentUsers(10).during(Duration.ofMinutes(5))
            );
            case MEDIUM -> scenario.injectClosed(
                    rampConcurrentUsers(0).to(25).during(Duration.ofMinutes(1)),
                    constantConcurrentUsers(25).during(Duration.ofMinutes(10))
            );
            case LARGE -> scenario.injectClosed(
                    rampConcurrentUsers(0).to(40).during(Duration.ofMinutes(2)),
                    constantConcurrentUsers(40).during(Duration.ofMinutes(15))
            );
        };
    }

    public static List<Assertion> assertionsFor(WorkloadType workloadType) {
        return switch (workloadType) {
            case BASIC -> List.of(
                    global().failedRequests().percent().lt(1.0),
                    global().responseTime().percentile3().lt(2000),
                    details("SmokeTest").responseTime().percentile3().lt(2000)
            );
            case MIXED_TPC -> List.of(
                    global().failedRequests().percent().lt(1.0),
                    global().responseTime().percentile3().lt(2000),
                    details("MixedWorkload", "NewOrder").responseTime().percentile3().lt(1500),
                    details("MixedWorkload", "Payment").responseTime().percentile3().lt(1000),
                    details("MixedWorkload", "OrderStatus").responseTime().percentile3().lt(1000),
                    details("MixedWorkload", "Delivery").responseTime().percentile3().lt(5000),
                    details("MixedWorkload", "StockLevel").responseTime().percentile3().lt(2000)
            );
            case NEW_ORDER -> assertionsForTransaction("NewOrder", 1500);
            case PAYMENT -> assertionsForTransaction("Payment", 1000);
            case ORDER_STATUS -> assertionsForTransaction("OrderStatus", 1000);
            case DELIVERY -> assertionsForTransaction("Delivery", 5000);
            case STOCK_LEVEL -> assertionsForTransaction("StockLevel", 2000);
        };
    }

    private static List<Assertion> assertionsForTransaction(String groupName, int percentile3LimitMs) {
        return List.of(
                global().failedRequests().percent().lt(1.0),
                global().responseTime().percentile3().lt(2000),
                details(groupName).responseTime().percentile3().lt(percentile3LimitMs));
    }
}
