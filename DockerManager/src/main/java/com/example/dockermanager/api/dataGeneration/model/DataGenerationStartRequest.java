package com.example.dockermanager.api.dataGeneration.model;

import com.example.dockermanager.domain.model.environment.DatabaseType;
import com.example.dockermanager.domain.model.environment.VolumeSize;
import lombok.Data;

@Data
public class DataGenerationStartRequest {
    private DatabaseType databaseType;
    private VolumeSize volumeType;
}
