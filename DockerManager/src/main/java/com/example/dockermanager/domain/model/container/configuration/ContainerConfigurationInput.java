package com.example.dockermanager.domain.model.container.configuration;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.environment.DatabaseType;
import com.example.dockermanager.domain.model.volume.VolumeType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class ContainerConfigurationInput {
    private final ContainerDefinition definition;
    private final VolumeType volumeType;
    private final DatabaseType databaseType;
    private final PerformanceTestsContainerParams performanceTestsContainerParams;
    private final DataGenerationContainerParams dataGenerationContainerParams;
}
