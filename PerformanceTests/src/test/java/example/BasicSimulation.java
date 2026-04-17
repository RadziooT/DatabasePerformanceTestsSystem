package example;

import static io.gatling.javaapi.core.CoreDsl.*;

import example.groups.ScenarioGroups;
import example.utils.Config;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.List;

@SuppressWarnings("unused")
public class BasicSimulation extends Simulation {

    private static final HttpProtocolBuilder httpProtocol = Config.httpProtocol();

    static final List<Assertion> assertions = List.of(global().failedRequests().count().lt(1L));

    private static final ScenarioBuilder scenario = scenario("Basic configuration simulation")
            .exitBlockOnFail()
            .on(ScenarioGroups.basicSmokeTest);

    {
        setUp(scenario.injectOpen(atOnceUsers(1)))
                .assertions(assertions)
                .protocols(httpProtocol);
    }
}
