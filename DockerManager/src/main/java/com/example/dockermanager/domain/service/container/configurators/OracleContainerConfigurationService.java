package com.example.dockermanager.domain.service.container.configurators;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.RuntimeConfigurationOverrideInput;
import com.example.dockermanager.domain.service.container.DockerNetworkService;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.HostConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class OracleContainerConfigurationService extends DatabaseContainerConfiguration {

    public OracleContainerConfigurationService(DockerNetworkService dockerNetworkService) {
        super(dockerNetworkService);
    }

    @Override
    protected Optional<String> volumeTargetPath() {
        return Optional.of("/opt/oracle/oradata");
    }

    @Override
    protected void applyAdditionalHostConfig(HostConfig hostConfig) {
        hostConfig.withShmSize(1_073_741_824L);
    }

    @Override
    protected void applyCustomContainerConfig(CreateContainerCmd createContainerCmd,
                                              ContainerDefinition definition,
                                              RuntimeConfigurationOverrideInput runtimeConfiguration) {
        createContainerCmd.withHealthcheck(buildHealthCheck(
                "/opt/oracle/healthcheck.sh || exit 1",
                ORACLE_HEALTHCHECK_START_PERIOD_NANOS
        ));
    }

    @Override
    protected Map<String, String> runtimeEnvironmentOverrides(RuntimeConfigurationOverrideInput runtimeConfiguration) {
        Map<String, String> overrides = new java.util.LinkedHashMap<>();
        overrides.put("ORACLE_SGA_TARGET", "2048M");
        overrides.put("ORACLE_PGA_AGGREGATE_TARGET", "512M");

        return overrides;
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.ORACLE;
    }
}
