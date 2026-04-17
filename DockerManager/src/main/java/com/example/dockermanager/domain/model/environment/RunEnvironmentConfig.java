package com.example.dockermanager.domain.model.environment;

public record RunEnvironmentConfig(
        DatabaseType databaseType,
        VolumeSize volumeType,
        boolean shutdownContainers,
        boolean removeContainers,
        boolean deleteVolumes
) {
}
