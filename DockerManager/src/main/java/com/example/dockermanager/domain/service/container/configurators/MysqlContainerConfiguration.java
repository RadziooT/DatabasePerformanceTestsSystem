package com.example.dockermanager.domain.service.container.configurators;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.RuntimeConfigurationOverrideInput;
import com.example.dockermanager.domain.service.container.DockerNetworkService;
import com.github.dockerjava.api.command.CreateContainerCmd;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MysqlContainerConfiguration extends DatabaseContainerConfiguration {

    public MysqlContainerConfiguration(DockerNetworkService dockerNetworkService) {
        super(dockerNetworkService);
    }

    @Override
    protected Optional<String> volumeTargetPath() {
        return Optional.of("/var/lib/mysql");
    }

    @Override
    protected void applyCustomContainerConfig(CreateContainerCmd createContainerCmd,
                                              ContainerDefinition definition,
                                              RuntimeConfigurationOverrideInput runtimeConfiguration) {
        createContainerCmd.withHealthcheck(buildHealthCheck("mysqladmin ping -h 127.0.0.1 -u root -p$MYSQL_ROOT_PASSWORD || exit 1"));

        // Required by data generation strategy that uses LOAD DATA LOCAL INFILE.
        createContainerCmd.withCmd("--local-infile=1");
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.MYSQL;
    }
}
