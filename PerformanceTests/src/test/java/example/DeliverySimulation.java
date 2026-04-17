package example;

import example.groups.ScenarioGroups;
import example.utils.Config;
import example.utils.WorkloadType;
import example.utils.SimulationConfiguration;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.scenario;

@SuppressWarnings("unused")
public class DeliverySimulation extends Simulation {

    private static final ScenarioBuilder transactionScenario = scenario("Delivery Scenario")
            .exitBlockOnFail()
            .on(ScenarioGroups.delivery);

    {
        setUp(SimulationConfiguration.injectionProfile(transactionScenario, WorkloadType.DELIVERY))
                .assertions(SimulationConfiguration.assertionsFor(WorkloadType.DELIVERY))
                .protocols(Config.httpProtocol());
    }
}
