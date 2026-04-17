package com.example.dockermanager.domain.service.container.configurators;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.PortMapping;
import com.github.dockerjava.api.model.*;
import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class ContainerConfigurationHelper {

    private static final String APP_LABEL_KEY = "app";
    private static final String APP_LABEL_VALUE = "docker-manager";
    private static final String MANAGED_BY_LABEL_KEY = "managed-by";
    private static final String MANAGED_BY_LABEL_VALUE = "docker-manager";
    public static final Map<String, String> MANAGED_CONTAINERS_LABEL_FILTER = Map.of(
            APP_LABEL_KEY, APP_LABEL_VALUE,
            MANAGED_BY_LABEL_KEY, MANAGED_BY_LABEL_VALUE
    );
    private static final String SERVICE_LABEL_KEY = "service";

    public List<PortMapping> parsePortMappings(ContainerDefinition definition) {
        List<String> ports = Optional.ofNullable(definition.getPorts()).orElse(List.of());
        List<PortMapping> mappings = new ArrayList<>();

        for (String rawMapping : ports) {
            String[] parts = rawMapping.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid port mapping for " + definition.getContainerName() + ": " + rawMapping);
            }

            try {
                int hostPort = Integer.parseInt(parts[0].trim());
                int containerPort = Integer.parseInt(parts[1].trim());
                mappings.add(new PortMapping(hostPort, containerPort));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid port mapping for " + definition.getContainerName() + ": " + rawMapping, ex);
            }
        }
        return mappings;
    }

    public List<ExposedPort> parseExposedPorts(List<PortMapping> mappings) {
        return mappings.stream()
                .map(mapping -> ExposedPort.tcp(mapping.containerPort()))
                .toList();
    }

    public void mountPorts(HostConfig hostConfig, List<PortMapping> mappings) {
        Ports portBindings = new Ports();
        for (PortMapping mapping : mappings) {
            ExposedPort exposedPort = ExposedPort.tcp(mapping.containerPort());
            Ports.Binding binding = new Ports.Binding("0.0.0.0", String.valueOf(mapping.hostPort()));
            portBindings.bind(exposedPort, binding);
        }
        if (!portBindings.getBindings().isEmpty()) {
            hostConfig.withPortBindings(portBindings);
        }
    }

    public List<Mount> buildMounts(ContainerDefinition definition,
                                   String dynamicVolumeName,
                                   Optional<String> dynamicMountTarget,
                                   String containerTypeName) {
        Map<String, Mount> mountsByTarget = new LinkedHashMap<>();
        boolean hasDynamicVolume = dynamicVolumeName != null && !dynamicVolumeName.isBlank();

        for (String volumeMapping : Optional.ofNullable(definition.getVolumes()).orElse(List.of())) {
            String[] parts = volumeMapping.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid volume mapping for " + definition.getContainerName() + ": " + volumeMapping);
            }

            String source = parts[0].trim();
            String target = parts[1].trim();
            mountsByTarget.put(target, volumeMount(source, target));
        }

        if (hasDynamicVolume) {
            String target = dynamicMountTarget.orElseThrow(() -> new IllegalStateException(
                    "Container type " + containerTypeName + " does not support dynamic data volume"
            ));
            mountsByTarget.put(target, volumeMount(dynamicVolumeName, target));
        }

        return new ArrayList<>(mountsByTarget.values());
    }

    public Map<String, String> buildLabels(ContainerDefinition definition, ContainerType containerType) {
        Map<String, String> labels = new HashMap<>();
        labels.put(APP_LABEL_KEY, APP_LABEL_VALUE);
        labels.put(MANAGED_BY_LABEL_KEY, MANAGED_BY_LABEL_VALUE);
        labels.put(SERVICE_LABEL_KEY, containerType.getContainerName());
        labels.putAll(Optional.ofNullable(definition.getLabels()).orElse(Map.of()));
        return labels;
    }

    public List<String> toEnvList(Map<String, String> environment) {
        return environment.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toList();
    }

    private Mount volumeMount(String source, String target) {
        return new Mount()
                .withType(MountType.VOLUME)
                .withSource(source)
                .withTarget(target)
                .withReadOnly(false);
    }
}
