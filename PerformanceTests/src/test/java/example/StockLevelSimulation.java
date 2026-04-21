package example;

import example.groups.ScenarioGroups;
import example.utils.Config;
import example.utils.SimulationConfiguration;
import example.utils.WorkloadType;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.scenario;

@SuppressWarnings("unused")
public class StockLevelSimulation extends Simulation {

    private static final ScenarioBuilder transactionScenario = scenario("Check Stock Level Scenario")
            .exitBlockOnFail()
            .on(ScenarioGroups.stockLevel);

    {
        setUp(SimulationConfiguration.injectionProfile(transactionScenario))
                .assertions(SimulationConfiguration.assertionsFor(WorkloadType.STOCK_LEVEL))
                .protocols(Config.httpProtocol());
    }
}
