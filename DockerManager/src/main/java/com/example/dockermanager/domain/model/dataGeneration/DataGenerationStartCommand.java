package com.example.dockermanager.domain.model.dataGeneration;

import com.example.dockermanager.domain.model.environment.DatabaseType;
import com.example.dockermanager.domain.model.environment.VolumeSize;

public record DataGenerationStartCommand(
        DatabaseType databaseType,
        VolumeSize volumeType
) {
}
