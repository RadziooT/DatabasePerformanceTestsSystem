package example;

import example.groups.ScenarioGroups;
import example.utils.Config;
import example.utils.WorkloadType;
import example.utils.SimulationConfiguration;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.scenario;

@SuppressWarnings("unused")
public class MixedTpccSimulation extends Simulation {

    private static final ScenarioBuilder mixedScenario = scenario("Mixed TPC-C Workload Scenario")
            .exitBlockOnFail()
            .on(ScenarioGroups.mixedWorkload);

    {
        setUp(SimulationConfiguration.injectionProfile(mixedScenario, WorkloadType.MIXED_TPC))
                .assertions(SimulationConfiguration.assertionsFor(WorkloadType.MIXED_TPC))
                .protocols(Config.httpProtocol());
    }
}
