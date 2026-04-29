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

    private static final long GATLING_MEMORY_LIMIT_BYTES = 2_147_483_648L; // 2GB
    private static final long GATLING_CPU_COUNT = 2L;

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
        overrides.put("JAVA_OPTS", "-Xms2g -Xmx2g");

        return overrides;
    }

    @Override
    protected void applyCustomHostConfig(HostConfig hostConfig) {
        hostConfig
                .withAutoRemove(true)
                .withMemory(GATLING_MEMORY_LIMIT_BYTES)
                .withMemorySwap(GATLING_MEMORY_LIMIT_BYTES)
                .withCpuCount(GATLING_CPU_COUNT)
                .withCpuPeriod(100_000L)
                .withCpuQuota(200_000L)
                .withRestartPolicy(RestartPolicy.noRestart());
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.GATLING;
    }
}
