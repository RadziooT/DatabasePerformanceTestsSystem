package com.example.dockermanager.domain.model.container.configuration;

public record DatabaseConnectionConfig(
        String host,
        int port,
        String databaseName,
        String user,
        String password
) {
}
