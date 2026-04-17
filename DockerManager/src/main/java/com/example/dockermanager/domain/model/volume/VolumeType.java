package com.example.dockermanager.domain.model.volume;

import lombok.Getter;

@Getter
public enum VolumeType {

    POSTGRES_SMALL("docker-manager-postgres-small"),
    POSTGRES_MEDIUM("docker-manager-postgres-medium"),
    POSTGRES_LARGE("docker-manager-postgres-large"),
    MYSQL_SMALL("docker-manager-mysql-small"),
    MYSQL_MEDIUM("docker-manager-mysql-medium"),
    MYSQL_LARGE("docker-manager-mysql-large"),
    SQLSERVER_SMALL("docker-manager-sqlserver-small"),
    SQLSERVER_MEDIUM("docker-manager-sqlserver-medium"),
    SQLSERVER_LARGE("docker-manager-sqlserver-large"),
    ORACLE_SMALL("docker-manager-oracle-small"),
    ORACLE_MEDIUM("docker-manager-oracle-medium"),
    ORACLE_LARGE("docker-manager-oracle-large"),
    GATLING("gatling-results-local");

    private final String volumeName;

    VolumeType(String volumeName) {
        this.volumeName = volumeName;
    }
}
