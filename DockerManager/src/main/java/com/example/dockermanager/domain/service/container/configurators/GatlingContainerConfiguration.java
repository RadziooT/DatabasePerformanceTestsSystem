package com.example.dockermanager.domain.service.container.configurators;

import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.RuntimeConfigurationOverrideInput;
import com.example.dockermanager.domain.service.container.DockerNetworkService;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.RestartPolicy;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class GatlingContainerConfiguration extends ContainerConfiguration {

    public GatlingContainerConfiguration(DockerNetworkService dockerNetworkService) {
        super(dockerNetworkService);
    }

    @Override
    protected Optional<String> volumeTargetPath() {
        return Optional.of("/app/target/gatling");
    }

    @Override
    protected Map<String, String> runtimeEnvironmentOverrides(RuntimeConfigurationOverrideInput runtimeConfiguration) {
        if (runtimeConfiguration.getPerformanceTestsContainerParams() == null) {
            return Map.of();
        }

        Map<String, String> overrides = new LinkedHashMap<>();
        overrides.put("SIMULATION_CLASS", runtimeConfiguration.getPerformanceTestsContainerParams().performanceTestSimulationType().getSimulationClass());
        overrides.put("DATASET_SIZE", runtimeConfiguration.getPerformanceTestsContainerParams().testedVolumeType().name());
        overrides.put("DATABASE_TYPE", runtimeConfiguration.getPerformanceTestsContainerParams().databaseType().name());

        return overrides;
    }

    @Override
    protected void applyCustomHostConfig(HostConfig hostConfig) {
        hostConfig
                .withAutoRemove(true)
                .withMemory(1_073_741_824L)
                .withCpuCount(2L)
                .withRestartPolicy(RestartPolicy.noRestart());
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.GATLING;
    }
}
