package com.example.dockermanager.api.environment.model;

import com.example.dockermanager.domain.model.environment.DatabaseType;
import com.example.dockermanager.domain.model.environment.VolumeSize;
import lombok.Data;

@Data
public class EnvironmentRunRequest {
    private DatabaseType databaseType;
    private VolumeSize databaseVolumeType;
    private boolean shutdownContainers;
    private boolean removeContainers;
    private boolean deleteVolumes;
}
