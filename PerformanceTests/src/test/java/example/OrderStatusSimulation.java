package example;

import example.groups.ScenarioGroups;
import example.utils.Config;
import example.utils.SimulationConfiguration;
import example.utils.WorkloadType;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.scenario;

@SuppressWarnings("unused")
public class OrderStatusSimulation extends Simulation {

    private static final ScenarioBuilder transactionScenario = scenario("Last Order Status Scenario")
            .exitBlockOnFail()
            .on(ScenarioGroups.orderStatus);

    {
        setUp(SimulationConfiguration.injectionProfile(transactionScenario))
                .assertions(SimulationConfiguration.assertionsFor(WorkloadType.ORDER_STATUS))
                .protocols(Config.httpProtocol());
    }
}
