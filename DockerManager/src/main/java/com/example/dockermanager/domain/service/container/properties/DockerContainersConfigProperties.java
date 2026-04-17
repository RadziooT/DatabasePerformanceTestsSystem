package com.example.dockermanager.domain.service.container.properties;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.SharedDatabaseCredentialsProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "docker")
@Getter
@Setter
public class DockerContainersConfigProperties {

    private final Map<ContainerType, ContainerDefinition> containers = new EnumMap<>(ContainerType.class);
    private SharedDatabaseCredentialsProperties sharedDb = new SharedDatabaseCredentialsProperties();
}
