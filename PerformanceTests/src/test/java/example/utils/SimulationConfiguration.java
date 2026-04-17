package example.utils;

import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import lombok.experimental.UtilityClass;

import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;

@UtilityClass
public final class SimulationConfiguration {

	public static PopulationBuilder injectionProfile(ScenarioBuilder scenario, WorkloadType workloadType) {
		return scenario.injectOpen(
				rampUsers(Config.userCountFor(workloadType)).during(30));
	}

	public static List<Assertion> assertionsFor(WorkloadType workloadType) {
        return switch (workloadType) {
			case BASIC -> latencyAssertions(2000, 3000, 5.0);
            case MIXED_TPC -> latencyAssertions(2000, 3500, 2.0);
            case NEW_ORDER -> latencyAssertions(2200, 3800, 2.0);
            case PAYMENT -> latencyAssertions(1600, 2600, 2.0);
            case ORDER_STATUS -> latencyAssertions(1000, 1800, 1.5);
            case DELIVERY -> latencyAssertions(3200, 5000, 2.5);
            case STOCK_LEVEL -> latencyAssertions(1200, 2100, 1.5);
        };
	}

	private static List<Assertion> latencyAssertions(int p95Ms, int p99Ms, double maxFailedPercent) {
		return List.of(
				global().responseTime().percentile(95.0).lt(p95Ms),
				global().responseTime().percentile(99.0).lt(p99Ms),
				global().failedRequests().percent().lt(maxFailedPercent));
	}
}
