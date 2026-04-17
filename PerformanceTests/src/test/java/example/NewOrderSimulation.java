package example;

import example.groups.ScenarioGroups;
import example.utils.Config;
import example.utils.WorkloadType;
import example.utils.SimulationConfiguration;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.scenario;

@SuppressWarnings("unused")
public class NewOrderSimulation extends Simulation {

    private static final ScenarioBuilder transactionScenario = scenario("New Order Creation Scenario")
            .exitBlockOnFail()
            .on(ScenarioGroups.newOrder);

    {
        setUp(SimulationConfiguration.injectionProfile(transactionScenario, WorkloadType.NEW_ORDER))
                .assertions(SimulationConfiguration.assertionsFor(WorkloadType.NEW_ORDER))
                .protocols(Config.httpProtocol());
    }
}
