package example;

import example.groups.ScenarioGroups;
import example.utils.Config;
import example.utils.SimulationConfiguration;
import example.utils.WorkloadType;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.scenario;

@SuppressWarnings("unused")
public class PaymentSimulation extends Simulation {

    private static final ScenarioBuilder transactionScenario = scenario("Register Payment Scenario")
            .exitBlockOnFail()
            .on(ScenarioGroups.payment);

    {
        setUp(SimulationConfiguration.injectionProfile(transactionScenario))
                .assertions(SimulationConfiguration.assertionsFor(WorkloadType.PAYMENT))
                .protocols(Config.httpProtocol());
    }
}
