package com.example.dockermanager.domain.service.util;

import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.environment.DatabaseType;
import com.example.dockermanager.domain.model.environment.VolumeSize;
import com.example.dockermanager.domain.model.volume.VolumeType;
import org.springframework.stereotype.Service;

@Service
public class DatabaseResourceMappingService {

    public ContainerType toContainerType(DatabaseType databaseType) {
        if (databaseType == null) {
            throw new IllegalArgumentException("containerDatabaseType is required");
        }
        return ContainerType.valueOf(databaseType.name());
    }

    public VolumeType toVolumeType(DatabaseType databaseType, VolumeSize volumeType) {
        if (databaseType == null) {
            throw new IllegalArgumentException("containerDatabaseType is required");
        }
        if (volumeType == null) {
            throw new IllegalArgumentException("volumeType is required");
        }
        return VolumeType.valueOf(databaseType.name() + "_" + volumeType.name());
    }
}
