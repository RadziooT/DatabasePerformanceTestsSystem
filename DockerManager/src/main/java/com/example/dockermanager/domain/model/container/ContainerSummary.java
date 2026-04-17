package com.example.dockermanager.domain.model.container;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ContainerSummary {
    String id;
    String name;
    String image;
    ContainerType containerType;
    ContainerState state;
    List<String> volumes;
    List<ContainerAction> availableActions;
}
