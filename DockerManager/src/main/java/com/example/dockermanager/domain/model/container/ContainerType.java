package com.example.dockermanager.domain.model.container;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ContainerType {

    POSTGRES("docker-manager-postgres"),
    MYSQL("docker-manager-mysql"),
    ORACLE("docker-manager-oracle"),
    SQLSERVER("docker-manager-sqlserver"),
    MOCK_APP("docker-manager-mock-app"),
    DATA_GENERATOR("docker-manager-data-generator"),
    GATLING("docker-manager-performance-tests");

    private final String containerName;

    public static ContainerType fromContainerName(String containerName) {
        return Arrays.stream(values())
                .filter(type -> type.containerName.equals(containerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported container name: " + containerName));
    }
}
