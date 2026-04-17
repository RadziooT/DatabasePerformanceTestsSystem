package com.example.dockermanager.domain.service.container;

import com.example.dockermanager.domain.exception.ContainerAlreadyExistsException;
import com.example.dockermanager.domain.exception.ContainerNotFoundException;
import com.example.dockermanager.domain.model.container.ContainerAction;
import com.example.dockermanager.domain.model.container.ContainerState;
import com.example.dockermanager.domain.model.container.ContainerSummary;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.ContainerConfigurationInput;
import com.example.dockermanager.domain.model.container.configuration.DataGenerationContainerParams;
import com.example.dockermanager.domain.model.container.configuration.PerformanceTestsContainerParams;
import com.example.dockermanager.domain.model.environment.DatabaseType;
import com.example.dockermanager.domain.model.volume.VolumeType;
import com.example.dockermanager.domain.service.container.configurators.ContainerConfigurationHelper;
import com.example.dockermanager.domain.service.container.properties.ContainerDefinitionService;
import com.example.dockermanager.domain.service.volume.VolumeService;
import com.example.dockermanager.domain.service.volume.VolumeUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerMount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContainerService {

    private final DockerClient dockerClient;
    private final ContainerDefinitionService containerDefinitionService;
    private final DockerContainerConfigurationFactory containerConfigurationFactory;
    private final VolumeUtil volumeUtil;
    private final VolumeService volumeService;

    public List<ContainerSummary> listContainers() {
        return containerDefinitionService.getContainerTypesNames().stream()
                .map(name -> {
                    var containerData = findContainerByName(name);
                    return containerData.isPresent()
                            ? mapPresentContainerInfo(containerData.get())
                            : mapEmptyContainerInfo(name);
                })
                .toList();
    }

    public void createMockAppContainer(ContainerType containerType, ContainerType dbContainerType) {
        var definition = containerDefinitionService.getDefinition(containerType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown container definition: " + containerType));
        var containerConfiguration = ContainerConfigurationInput.of(
                definition,
                null,
                DatabaseType.valueOf(dbContainerType.name().toUpperCase()),
                null,
                null
        );

        createContainer(containerType, null, containerConfiguration);
    }

    public void createDatabaseContainer(ContainerType containerType, VolumeType volumeType) {
        var definition = containerDefinitionService.getDefinition(containerType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown container definition: " + containerType));
        var containerConfiguration = ContainerConfigurationInput.of(
                definition,
                volumeType,
                null,
                null,
                null
        );

        createContainer(containerType, volumeType, containerConfiguration);
    }

    public void createDataGenerationContainer(ContainerType containerType, DataGenerationContainerParams dataGenerationContainerParams) {
        var definition = containerDefinitionService.getDefinition(containerType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown container definition: " + containerType));
        var containerConfiguration = ContainerConfigurationInput.of(
                definition,
                null,
                null,
                null,
                dataGenerationContainerParams
        );

        createContainer(containerType, null, containerConfiguration);
    }

    public void createPerformanceTestsContainer(ContainerType containerType,
                                                VolumeType volumeType,
                                                PerformanceTestsContainerParams performanceTestsContainerParams) {
        var definition = containerDefinitionService.getDefinition(containerType)
                .orElseThrow(() -> new IllegalArgumentException("Unknown container definition: " + containerType));
        var testsContainerParams = new PerformanceTestsContainerParams(
                performanceTestsContainerParams.performanceTestSimulationType(),
                performanceTestsContainerParams.testedVolumeType(),
                performanceTestsContainerParams.databaseType());

        var containerConfiguration = ContainerConfigurationInput.of(
                definition,
                volumeType,
                null,
                testsContainerParams,
                null
        );
        createContainer(containerType, volumeType, containerConfiguration);
    }

    private void createContainer(ContainerType containerType, VolumeType volumeType,
                                 ContainerConfigurationInput runtimeConfiguration) {
        var definition = runtimeConfiguration.getDefinition();
        var containerConfiguration = containerConfigurationFactory.forType(containerType);

        checkImageExists(definition.getImage());
        checkVolumeExists(volumeType);

        try {
            var createContainerCmd = dockerClient.createContainerCmd(definition.getImage());
            containerConfiguration.applyContainerConfiguration(createContainerCmd, runtimeConfiguration);
            createContainerCmd.exec();
        } catch (ConflictException e) {
            throw new ContainerAlreadyExistsException("Container already exists: " + definition.getContainerName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create container: " + definition.getContainerName(), e);
        }
    }

    private void checkImageExists(String image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Provided image has to be created manually: " + image);
        }

        try {
            dockerClient.inspectImageCmd(image).exec();
        } catch (NotFoundException e) {
            throw new RuntimeException("Image doesn't exist");
        }
    }

    private void checkVolumeExists(VolumeType volumeType) {
        if (volumeType != null) {
            if (!volumeUtil.isVolumePresent(volumeType)) {
                volumeService.createVolume(volumeType);
            }
        }
    }

    public void startContainer(ContainerType containerType) {
        findContainerByName(containerType.getContainerName()).ifPresentOrElse(
                container -> {
                    log.info("Starting container {} ({})", container.getNames()[0], container.getId());
                    dockerClient.startContainerCmd(container.getId()).exec();
                },
                () -> {
                    throw new ContainerNotFoundException("Container not found: " + containerType.getContainerName());
                }
        );
    }

    public boolean isContainerCreated(ContainerType containerType) {
        return findContainerByName(containerType.getContainerName()).isPresent();
    }

    public boolean isContainerRunning(ContainerType containerType) {
        return findContainerByName(containerType.getContainerName())
                .map(container -> "running".equals(container.getState()))
                .orElse(false);
    }

    public void waitForContainerReady(ContainerType containerType, Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);

        while (Instant.now().isBefore(deadline)) {
            Optional<Container> container = findContainerByName(containerType.getContainerName());
            if (container.isPresent()) {
                var inspect = dockerClient.inspectContainerCmd(container.get().getId()).exec();
                var state = inspect.getState();
                boolean running = state != null && Boolean.TRUE.equals(state.getRunning());
                String healthStatus = state != null && state.getHealth() != null ? state.getHealth().getStatus() : null;
                boolean hasHealthCheck = inspect.getConfig() != null && inspect.getConfig().getHealthcheck() != null;

                if (running) {
                    if (hasHealthCheck) {
                        if ("healthy".equalsIgnoreCase(healthStatus)) {
                            return;
                        } else if ("unhealthy".equalsIgnoreCase(healthStatus)) {
                            throw new IllegalStateException("Container became unhealthy: " + containerType.getContainerName());
                        }
                    } else {
                        return;
                    }
                }
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for container readiness: " + containerType.getContainerName(), e);
            }
        }

        throw new IllegalStateException("Timed out waiting for container readiness: " + containerType.getContainerName());
    }

    public boolean isVolumeMounted(ContainerType containerType, VolumeType volumeType) {
        if (volumeType == null) {
            return false;
        }

        return findContainerByName(containerType.getContainerName())
                .map(container -> Optional.ofNullable(container.getMounts()).orElse(List.of()))
                .stream()
                .flatMap(List::stream)
                .map(ContainerMount::getName)
                .filter(Objects::nonNull)
                .anyMatch(volumeType.getVolumeName()::equals);
    }

    public void stopContainer(ContainerType containerType) {
        findContainerByName(containerType.getContainerName()).ifPresentOrElse(
                container -> {
                    var state = container.getState();
                    if ("running".equals(state)) {
                        log.info("Stopping container {} ({})", container.getNames()[0], container.getId());
                        dockerClient.stopContainerCmd(container.getId()).exec();
                    }
                },
                () -> {
                    throw new ContainerNotFoundException("Container not found: " + containerType.getContainerName());
                }
        );
    }

    public void stopAllContainers() {
        findManagedContainers().forEach(container -> {
            var state = container.getState();
            if ("running".equals(state)) {
                dockerClient.stopContainerCmd(container.getId()).exec();
            }
        });
    }

    public void removeContainer(ContainerType containerType) {
        findContainerByName(containerType.getContainerName()).ifPresentOrElse(
                container -> {
                    log.info("Removing container {} ({})", container.getNames()[0], container.getId());
                    dockerClient.removeContainerCmd(container.getId()).exec();
                },
                () -> {
                    throw new ContainerNotFoundException("Container not found: " + containerType.getContainerName());
                }
        );
    }

    public void removeAllContainers() {
        findManagedContainers().forEach(container -> {
            dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
        });
    }

    private ContainerSummary mapPresentContainerInfo(Container containerApiData) {
        var state = containerApiData.getState();
        List<ContainerAction> availableActions = new ArrayList<>();
        if ("running".equals(state)) {
            availableActions.add(ContainerAction.STOP);
        }
        if (List.of("created", "paused", "stopped", "exited").contains(state)) {
            availableActions.add(ContainerAction.DELETE);
        }

        return ContainerSummary.builder()
                .id(containerApiData.getId())
                .name(Arrays.stream(containerApiData.getNames())
                        .map(String::trim)
                        .findFirst()
                        .map(this::cleanContainerName)
                        .orElse("Name not found"))
                .image(containerApiData.getImage())
                .containerType(resolveContainerType(this.cleanContainerName(containerApiData.getNames()[0])))
                .state(ContainerState.valueOf(containerApiData.getState().toUpperCase()))
                .volumes(containerApiData.getMounts().stream().map(ContainerMount::getName).toList())
                .availableActions(availableActions)
                .build();
    }

    private ContainerSummary mapEmptyContainerInfo(String containerName) {
        return ContainerSummary.builder()
                .name(containerName)
                .containerType(resolveContainerType(containerName))
                .state(ContainerState.NOT_CREATED)
                .availableActions(List.of())
                .build();
    }

    private ContainerType resolveContainerType(String containerName) {
        return ContainerType.fromContainerName(containerName);
    }

    private Optional<Container> findContainerByName(String name) {
        return findManagedContainers().stream()
                .filter(container -> Arrays.stream(container.getNames())
                        .map(this::cleanContainerName)
                        .anyMatch(n -> n.equals(name)))
                .findFirst();
    }

    private String cleanContainerName(String name) {
        return name.startsWith("/") ? name.substring(1) : name;
    }

    private List<Container> findManagedContainers() {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withLabelFilter(ContainerConfigurationHelper.MANAGED_CONTAINERS_LABEL_FILTER)
                .exec();
    }
}
