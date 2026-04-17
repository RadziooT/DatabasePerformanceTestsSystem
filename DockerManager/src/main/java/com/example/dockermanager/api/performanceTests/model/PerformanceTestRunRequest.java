package com.example.dockermanager.api.performanceTests.model;

import com.example.dockermanager.domain.model.environment.DatabaseType;
import com.example.dockermanager.domain.model.environment.PerformanceSimulationType;
import com.example.dockermanager.domain.model.environment.VolumeSize;
import lombok.Data;

@Data
public class PerformanceTestRunRequest {
    private PerformanceSimulationType simulationType;
    private VolumeSize volumeSize;
    private DatabaseType databaseType;
}
