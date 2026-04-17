package com.example.dockermanager.domain.model.container.configuration;

import com.example.dockermanager.domain.model.environment.DatabaseType;
import com.example.dockermanager.domain.model.environment.PerformanceSimulationType;
import com.example.dockermanager.domain.model.environment.VolumeSize;

public record PerformanceTestsContainerParams(
        PerformanceSimulationType performanceTestSimulationType,
        VolumeSize testedVolumeType,
        DatabaseType databaseType
) {
}
