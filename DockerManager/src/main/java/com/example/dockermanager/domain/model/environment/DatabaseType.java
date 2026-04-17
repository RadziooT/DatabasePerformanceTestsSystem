package com.example.dockermanager.domain.model.environment;

import com.example.dockermanager.domain.model.container.ContainerType;

import java.util.Arrays;
import java.util.List;

public enum DatabaseType {
    MYSQL,
    POSTGRES,
    ORACLE,
    SQLSERVER;

    public static List<String> getNames() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }

    public ContainerType toContainerType() {
        return switch (this) {
            case MYSQL -> ContainerType.MYSQL;
            case POSTGRES -> ContainerType.POSTGRES;
            case ORACLE -> ContainerType.ORACLE;
            case SQLSERVER -> ContainerType.SQLSERVER;
        };
    }
}
