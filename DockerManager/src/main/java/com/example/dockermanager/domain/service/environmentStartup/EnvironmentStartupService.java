package com.example.dockermanager.domain.service.environmentStartup;

import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.PerformanceTestsContainerParams;
import com.example.dockermanager.domain.model.environment.DatabaseType;
import com.example.dockermanager.domain.model.environment.PerformanceSimulationType;
import com.example.dockermanager.domain.model.environment.RunEnvironmentConfig;
import com.example.dockermanager.domain.model.environment.VolumeSize;
import com.example.dockermanager.domain.model.volume.VolumeType;
import com.example.dockermanager.domain.service.container.ContainerService;
import com.example.dockermanager.domain.service.util.DatabaseResourceMappingService;
import com.example.dockermanager.domain.service.volume.VolumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnvironmentStartupService {

    private final ContainerService containerService;
    private final VolumeService volumeService;
    private final DatabaseResourceMappingService databaseResourceMappingService;

    public void startEnvironment(RunEnvironmentConfig request) {
        if (request.shutdownContainers()) {
            containerService.stopAllContainers();
        }

        if (request.deleteVolumes()) {
            volumeService.deleteAllVolumes();
        }

        if (request.removeContainers()) {
            containerService.removeAllContainers();
        }

        // Database container creation
        var mappedDbContainerType = databaseResourceMappingService.toContainerType(request.databaseType());

        VolumeType mappedVolumeType = databaseResourceMappingService.toVolumeType(
                request.databaseType(),
                request.volumeType()
        );
        containerService.createDatabaseContainer(mappedDbContainerType, mappedVolumeType);

        // MockApp container creation
        if (containerService.isContainerRunning(ContainerType.MOCK_APP)) {
            containerService.stopContainer(ContainerType.MOCK_APP);
        }
        if (containerService.isContainerCreated(ContainerType.MOCK_APP)) {
            containerService.removeContainer(ContainerType.MOCK_APP);
        }
        containerService.createMockAppContainer(ContainerType.MOCK_APP, mappedDbContainerType);

        // Start containers
        containerService.startContainer(mappedDbContainerType);
        containerService.waitForContainerReady(mappedDbContainerType, Duration.ofSeconds(90));
        containerService.startContainer(ContainerType.MOCK_APP);
    }

    public void startPerformanceTestsSequence(PerformanceSimulationType simulationType,
                                              VolumeSize testVolumeSize,
                                              DatabaseType databaseType) {
        if (simulationType == null) {
            throw new IllegalArgumentException("simulationType is required");
        }
        if (databaseType == null) {
            throw new IllegalArgumentException("databaseType is required");
        }
        var containerParams = new PerformanceTestsContainerParams(simulationType, testVolumeSize, databaseType);

        containerService.createPerformanceTestsContainer(ContainerType.GATLING, VolumeType.GATLING, containerParams);
        containerService.startContainer(ContainerType.GATLING);
    }
}
