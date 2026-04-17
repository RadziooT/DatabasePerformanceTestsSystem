package com.example.dockermanager.api.environment.mapper;

import com.example.dockermanager.api.environment.model.EnvironmentRunRequest;
import com.example.dockermanager.domain.model.environment.RunEnvironmentConfig;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EnvironmentRequestMapper {

    public RunEnvironmentConfig toRunEnvironmentConfig(EnvironmentRunRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        return new RunEnvironmentConfig(
                request.getDatabaseType(),
                request.getDatabaseVolumeType(),
                request.isShutdownContainers(),
                request.isRemoveContainers(),
                request.isDeleteVolumes()
        );
    }
}
