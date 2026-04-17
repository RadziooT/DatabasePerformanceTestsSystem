package com.example.dockermanager.domain.model.container;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ContainerDefinition {
    private String containerName;
    private String image;
    private List<String> ports;
    private Map<String, String> environment;
    private Map<String, String> labels;
    private List<String> volumes;

    public ContainerDefinition(ContainerDefinition source) {
        this.containerName = source.containerName;
        this.image = source.image;
        this.ports = source.ports;
        this.environment = source.environment;
        this.labels = source.labels;
        this.volumes = source.volumes;
    }
}
