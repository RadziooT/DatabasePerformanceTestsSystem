package com.example.dockermanager.domain.model.container.configuration;

import com.example.dockermanager.domain.model.environment.DatabaseType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RuntimeConfigurationOverrideInput {
    private DatabaseType databaseType;
    private PerformanceTestsContainerParams performanceTestsContainerParams;
    private DataGenerationContainerParams dataGenerationContainerParams;
}
