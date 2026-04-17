package com.example.dockermanager.domain.model.container.configuration;

import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.environment.VolumeSize;

public record DataGenerationContainerParams(
        String jobId,
        ContainerType databaseType,
        VolumeSize volumeType
) {
}
