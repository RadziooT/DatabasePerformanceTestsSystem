package com.example.dockermanager.domain.service.container.configurators;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.ContainerConfigurationInput;
import com.example.dockermanager.domain.model.container.configuration.RuntimeConfigurationOverrideInput;
import com.example.dockermanager.domain.model.volume.VolumeType;
import com.example.dockermanager.domain.service.container.DockerNetworkService;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class ContainerConfiguration {

    private final DockerNetworkService dockerNetworkService;

    public final void applyContainerConfiguration(CreateContainerCmd createContainerCmd,
                                                  ContainerConfigurationInput input) {
        ContainerDefinition definition = input.getDefinition();

        createContainerCmd.withName(definition.getContainerName())
                .withHostConfig(applyHostConfig(definition, input.getVolumeType()));

        RuntimeConfigurationOverrideInput runtimeConfiguration = new RuntimeConfigurationOverrideInput(
                input.getDatabaseType(),
                input.getPerformanceTestsContainerParams(),
                input.getDataGenerationContainerParams()
        );

        Map<String, String> environment = new LinkedHashMap<>(Optional.ofNullable(definition.getEnvironment()).orElse(Map.of()));
        environment.putAll(runtimeEnvironmentOverrides(runtimeConfiguration));
        List<String> envVars = ContainerConfigurationHelper.toEnvList(environment);

        Map<String, String> labels = ContainerConfigurationHelper.buildLabels(definition, getContainerType());
        List<ExposedPort> exposedPorts = ContainerConfigurationHelper.parseExposedPorts(
                ContainerConfigurationHelper.parsePortMappings(definition)
        );
        createContainerCmd
                .withEnv(envVars)
                .withLabels(labels)
                .withExposedPorts(exposedPorts)
                .withHostName(definition.getContainerName());

        applyCustomContainerConfig(createContainerCmd, definition, runtimeConfiguration);
    }

    private HostConfig applyHostConfig(ContainerDefinition definition, VolumeType volumeType) {
        HostConfig hostConfig = new HostConfig();
        var portMappings = ContainerConfigurationHelper.parsePortMappings(definition);

        applyDefaultHostConfig(hostConfig);
        ContainerConfigurationHelper.mountPorts(hostConfig, portMappings);
        mountVolumes(hostConfig, definition, volumeType == null ? null : volumeType.getVolumeName());
        dockerNetworkService.withCustomSharedNetwork(hostConfig);
        applyCustomHostConfig(hostConfig);
        return hostConfig;
    }

    private void applyDefaultHostConfig(HostConfig hostConfig) {
        hostConfig.withInit(true);
    }

    private void mountVolumes(HostConfig hostConfig, ContainerDefinition definition, String volumeName) {
        List<Mount> mounts = ContainerConfigurationHelper.buildMounts(
                definition,
                volumeName,
                volumeTargetPath(),
                getContainerType().name()
        );
        if (!mounts.isEmpty()) {
            hostConfig.withMounts(mounts);
        }
    }

    protected Optional<String> volumeTargetPath() {
        return Optional.empty();
    }

    protected Map<String, String> runtimeEnvironmentOverrides(RuntimeConfigurationOverrideInput runtimeConfiguration) {
        return Map.of();
    }

    protected void applyCustomHostConfig(HostConfig hostConfig) {
        // No-op by default
    }

    protected void applyCustomContainerConfig(CreateContainerCmd createContainerCmd,
                                              ContainerDefinition definition,
                                              RuntimeConfigurationOverrideInput runtimeConfiguration) {
        // No-op by default
    }

    public abstract ContainerType getContainerType();
}
