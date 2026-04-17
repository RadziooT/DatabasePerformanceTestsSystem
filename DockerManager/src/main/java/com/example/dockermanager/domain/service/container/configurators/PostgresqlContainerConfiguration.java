package com.example.dockermanager.domain.service.container.configurators;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.RuntimeConfigurationOverrideInput;
import com.example.dockermanager.domain.service.container.DockerNetworkService;
import com.github.dockerjava.api.command.CreateContainerCmd;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PostgresqlContainerConfiguration extends DatabaseContainerConfiguration {

    public PostgresqlContainerConfiguration(DockerNetworkService dockerNetworkService) {
        super(dockerNetworkService);
    }

    @Override
    protected Optional<String> volumeTargetPath() {
        return Optional.of("/var/lib/postgresql/data");
    }

    @Override
    protected void applyCustomContainerConfig(CreateContainerCmd createContainerCmd,
                                              ContainerDefinition definition,
                                              RuntimeConfigurationOverrideInput runtimeConfiguration) {
        createContainerCmd.withHealthcheck(buildHealthCheck("pg_isready -d $POSTGRES_DB -U $POSTGRES_USER || exit 1"));
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.POSTGRES;
    }
}
