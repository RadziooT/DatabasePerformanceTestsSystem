package example;

import example.groups.ScenarioGroups;
import example.utils.Config;
import example.utils.SimulationConfiguration;
import example.utils.WorkloadType;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.scenario;

@SuppressWarnings("unused")
public class BasicSimulation extends Simulation {

    private static final HttpProtocolBuilder httpProtocol = Config.httpProtocol();

    private static final ScenarioBuilder scenario = scenario("Basic configuration simulation")
            .exitBlockOnFail()
            .on(ScenarioGroups.basicSmokeTest);

    {
        setUp(SimulationConfiguration.injectionProfile(scenario))
                .assertions(SimulationConfiguration.assertionsFor(WorkloadType.BASIC))
                .protocols(httpProtocol);
    }
}
